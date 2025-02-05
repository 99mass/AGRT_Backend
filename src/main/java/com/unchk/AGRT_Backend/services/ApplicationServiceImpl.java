package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.config.JwtProperties;
import com.unchk.AGRT_Backend.dto.ApplicationDTO;
import com.unchk.AGRT_Backend.dto.ApplicationWithDocumentsDTO;
import com.unchk.AGRT_Backend.dto.DocumentDTO;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.*;
import com.unchk.AGRT_Backend.repositories.*;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobAnnouncementRepository announcementRepository;
    private final AcademicYearRepository academicYearRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;
    // private final NotificationService notificationService;
    private final JwtProperties jwtProperties;

    private static final String UPLOAD_DIR = "uploads/documents";

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        if (token == null) {
            throw new UserServiceException("Invalid authentication token", HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.get("email", String.class);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserServiceException("User not found", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Error processing authentication token", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public ApplicationDTO createApplicationWithDocuments(ApplicationWithDocumentsDTO applicationWithDocumentsDTO) {

        // Vérifier si le candidat existe
        User candidate;
        try {
            candidate = userRepository.findById(applicationWithDocumentsDTO.getCandidateId())
                    .orElseThrow(() -> new UserServiceException("Candidat non trouvé", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Candidat non trouvé", HttpStatus.NOT_FOUND);
        }

        // Vérifier si l'annonce existe
        JobAnnouncement announcement;
        try {
            announcement = announcementRepository.findById(applicationWithDocumentsDTO.getAnnouncementId())
                    .orElseThrow(() -> new UserServiceException("Annonce non trouvée", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Annonce non trouvée", HttpStatus.NOT_FOUND);
        }

        // Vérifier si l'année académique existe
        try {
            @SuppressWarnings("unused")
            AcademicYear academicYear = academicYearRepository.findById(applicationWithDocumentsDTO.getAcademicYearId())
                    .orElseThrow(() -> new UserServiceException("Année académique non trouvée", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Année académique non trouvée", HttpStatus.NOT_FOUND);
        }

        // Vérifier si l'annonce est ouverte
        if (!announcement.isOpen()) {
            throw new UserServiceException("Cette annonce n'est plus ouverte aux candidatures", HttpStatus.BAD_REQUEST);
        }

        // Vérifier si le candidat peut postuler
        if (!canCandidateApply(candidate.getId(), announcement.getId())) {
            throw new UserServiceException("Vous avez déjà postulé à cette annonce", HttpStatus.BAD_REQUEST);
        }

        // Créer la candidature
        Application application = new Application();
        application.setCandidate(candidate);
        application.setAnnouncement(announcement);
        application.setAcademicYear(announcement.getAcademicYear());
        application.setApplicationType(applicationWithDocumentsDTO.getApplicationType());
        application.setStatus(ApplicationStatus.PENDING);

        // Sauvegarder la candidature
        Application savedApplication;
        try {
            savedApplication = applicationRepository.save(application);
        } catch (Exception e) {
            throw new UserServiceException("Erreur lors de la sauvegarde de la candidature",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Ajouter les documents
        for (DocumentDTO documentDTO : applicationWithDocumentsDTO.getDocuments()) {
            addDocumentToApplication(savedApplication.getId(), documentDTO.getBase64Content(),
                    documentDTO.getOriginalFilename(), documentDTO.getDocumentType());
        }

        // Recharger l'application pour le mapping
        Application freshApplication;
        try {
            freshApplication = applicationRepository.findById(savedApplication.getId())
                    .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Erreur lors du rechargement de la candidature",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return modelMapper.map(freshApplication, ApplicationDTO.class);
    }

    @Override
    @Transactional
    public ApplicationDTO updateApplicationStatus(UUID id, ApplicationStatus newStatus, String comments) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("Application not found", HttpStatus.NOT_FOUND));

        // Vérifier si le changement de statut est valide
        if (application.getStatus() == newStatus) {
            return modelMapper.map(application, ApplicationDTO.class);
        }

        // Get current user and update status
        User currentUser = getCurrentUser();
        application.updateStatus(newStatus, currentUser, comments);
        Application updatedApplication = applicationRepository.save(application);

        // Envoyer une notification
        // notificationService.sendStatusUpdateNotification(updatedApplication);

        return modelMapper.map(updatedApplication, ApplicationDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDTO getApplicationById(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));
        return modelMapper.map(application, ApplicationDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(app -> modelMapper.map(app, ApplicationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplicationsByCandidate(UUID candidateId) {
        return applicationRepository.findByCandidateId(candidateId).stream()
                .map(app -> modelMapper.map(app, ApplicationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplicationsByAnnouncement(UUID announcementId) {
        return applicationRepository.findByAnnouncementId(announcementId).stream()
                .map(app -> modelMapper.map(app, ApplicationDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addDocumentToApplication(UUID applicationId, String base64File, String originalFilename,
            DocumentType documentType) {
        Application application;
        try {
            application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));
        } catch (UserServiceException e) {
            throw new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND);
        }

        if (!application.canBeUpdated()) {
            throw new UserServiceException("La candidature ne peut plus être modifiée", HttpStatus.BAD_REQUEST);
        }

        try {
            // Décoder le contenu base64
            String[] parts = base64File.split(",");
            String base64Content = parts.length > 1 ? parts[1] : parts[0];
            byte[] fileContent = Base64.getDecoder().decode(base64Content);

            // Créer le document
            Document document = new Document();
            document.setApplication(application);
            document.setDocumentType(documentType);
            document.setFileName(originalFilename);
            document.setFileSize(fileContent.length);
            document.setMimeType("application/pdf");

            // Générer le chemin du fichier AVANT de sauvegarder
            document.generateFilePath();

            // Sauvegarder le document
            document = documentRepository.save(document);

            // Vérifier et créer le répertoire si nécessaire
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Sauvegarder le fichier physiquement
            Path filePath = uploadPath.resolve(document.getFilePath());
            Files.write(filePath, fileContent);

            // Validation du document
            try {
                document.validate();
            } catch (IllegalArgumentException e) {
                // En cas d'erreur, supprimer le fichier physique
                Files.deleteIfExists(filePath);
                throw new UserServiceException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            throw new UserServiceException("Erreur lors de l'upload du document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void removeDocumentFromApplication(UUID applicationId, UUID documentId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));

        if (!application.canBeUpdated()) {
            throw new UserServiceException("La candidature ne peut plus être modifiée", HttpStatus.BAD_REQUEST);
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserServiceException("Document non trouvé", HttpStatus.NOT_FOUND));

        if (!document.getApplication().getId().equals(applicationId)) {
            throw new UserServiceException("Le document n'appartient pas à cette candidature", HttpStatus.BAD_REQUEST);
        }

        try {
            // Supprimer le fichier physique
            Path filePath = Paths.get(UPLOAD_DIR, document.getFilePath());
            Files.deleteIfExists(filePath);

            // Supprimer de la base de données
            application.removeDocument(document);
            documentRepository.delete(document);
            applicationRepository.save(application);

        } catch (IOException e) {
            throw new UserServiceException("Erreur lors de la suppression du document",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getApplicationDocuments(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));
        return application.getDocuments().stream().collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplicationComplete(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new UserServiceException("Candidature non trouvée", HttpStatus.NOT_FOUND));
        return application.isComplete() && application.validateDocuments();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCandidateApply(UUID candidateId, UUID announcementId) {
        return !applicationRepository.existsByCandidateIdAndAnnouncementId(candidateId, announcementId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplicationsByAcademicYear(UUID academicYearId) {
        return applicationRepository.findByAcademicYearId(academicYearId).stream()
                .map(app -> modelMapper.map(app, ApplicationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDTO> searchApplications(String query) {
        // Recherche dans le prénom, nom, ou email du candidat
        return applicationRepository.searchApplications(query).stream()
                .map(app -> modelMapper.map(app, ApplicationDTO.class))
                .collect(Collectors.toList());
    }

}