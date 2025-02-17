package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.config.JwtProperties;
import com.unchk.AGRT_Backend.dto.JobAnnouncementDTO;
import com.unchk.AGRT_Backend.enums.AnnouncementStatus;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.AcademicYear;
import com.unchk.AGRT_Backend.models.JobAnnouncement;
import com.unchk.AGRT_Backend.models.User;
import com.unchk.AGRT_Backend.repositories.AcademicYearRepository;
import com.unchk.AGRT_Backend.repositories.JobAnnouncementRepository;
import com.unchk.AGRT_Backend.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobAnnouncementServiceImpl implements JobAnnouncementService {

    private final JobAnnouncementRepository announcementRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    private JwtProperties jwtProperties;

    private void verifyAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentication requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        if (token != null) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtProperties.getSecretKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                String role = claims.get("role", String.class);
                if (!"ADMIN".equals(role)) {
                    throw new UserServiceException("Accès restreint aux administrateurs", HttpStatus.FORBIDDEN);
                }
            } catch (Exception e) {
                throw new UserServiceException("Erreur d'authentification", HttpStatus.FORBIDDEN);
            }
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserServiceException("Authentication requise", HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication.getCredentials() instanceof String) {
            token = (String) authentication.getCredentials();
        }

        if (token == null) {
            throw new UserServiceException("Token d'authentification invalide", HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.get("email", String.class);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserServiceException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            throw new UserServiceException("Erreur lors du traitement du token", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public JobAnnouncementDTO createAnnouncement(JobAnnouncementDTO announcementDTO) {
        verifyAdminAccess();

        // Vérifier si une annonce avec le même titre existe déjà
        if (announcementRepository.existsByTitleAndAcademicYear_Id(
                announcementDTO.getTitle(),
                announcementDTO.getAcademicYearId())) {
            throw new UserServiceException(
                    "Une annonce avec ce titre existe déjà pour cette année académique",
                    HttpStatus.BAD_REQUEST);
        }

        // Validate academic year
        AcademicYear academicYear = academicYearRepository.findById(announcementDTO.getAcademicYearId())
                .orElseThrow(() -> new UserServiceException("Academic year not found", HttpStatus.NOT_FOUND));

        // Récupérer l'utilisateur courant à partir du JWT
        User currentUser = getCurrentUser();

        try {
            JobAnnouncement announcement = modelMapper.map(announcementDTO, JobAnnouncement.class);
            announcement.setAcademicYear(academicYear);
            announcement.setCreatedBy(currentUser); // Utiliser l'utilisateur courant
            announcement.setStatus(AnnouncementStatus.DRAFT);

            JobAnnouncement savedAnnouncement = announcementRepository.save(announcement);
            return modelMapper.map(savedAnnouncement, JobAnnouncementDTO.class);
        } catch (Exception e) {
            throw new UserServiceException("Error creating job announcement: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<JobAnnouncementDTO> getAllAnnouncements() {
        try {
            List<JobAnnouncement> announcements = announcementRepository.findAll();
            return announcements.stream()
                    .map(announcement -> modelMapper.map(announcement, JobAnnouncementDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new UserServiceException("Error retrieving job announcements",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public JobAnnouncementDTO getAnnouncementById(UUID id) {
        JobAnnouncement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("Job announcement not found", HttpStatus.NOT_FOUND));
        try {
            return modelMapper.map(announcement, JobAnnouncementDTO.class);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new UserServiceException("Error retrieving job announcement",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public JobAnnouncementDTO updateAnnouncement(UUID id, JobAnnouncementDTO announcementDTO) {
        verifyAdminAccess();

        JobAnnouncement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("Job announcement not found", HttpStatus.NOT_FOUND));
        try {

            if (announcementDTO.getAcademicYearId() != null) {
                AcademicYear academicYear = academicYearRepository.findById(announcementDTO.getAcademicYearId())
                        .orElseThrow(() -> new UserServiceException("Academic year not found", HttpStatus.NOT_FOUND));
                existingAnnouncement.setAcademicYear(academicYear);
            }

            // Update fields
            if (announcementDTO.getTitle() != null) {
                existingAnnouncement.setTitle(announcementDTO.getTitle());
            }
            if (announcementDTO.getDescription() != null) {
                existingAnnouncement.setDescription(announcementDTO.getDescription());
            }
            if (announcementDTO.getClosingDate() != null) {
                existingAnnouncement.setClosingDate(announcementDTO.getClosingDate());
            }
            if (announcementDTO.getStatus() != null) {
                existingAnnouncement.setStatus(announcementDTO.getStatus());
            }

            JobAnnouncement updatedAnnouncement = announcementRepository.save(existingAnnouncement);
            return modelMapper.map(updatedAnnouncement, JobAnnouncementDTO.class);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new UserServiceException("Error updating job announcement",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteAnnouncement(UUID id) {
        verifyAdminAccess();
        try {
            if (!announcementRepository.existsById(id)) {
                throw new UserServiceException("Job announcement not found", HttpStatus.NOT_FOUND);
            }
            announcementRepository.deleteById(id);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new UserServiceException("Error deleting job announcement",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}