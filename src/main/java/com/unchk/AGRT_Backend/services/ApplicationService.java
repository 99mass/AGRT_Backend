package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.dto.ApplicationDTO;
import com.unchk.AGRT_Backend.dto.ApplicationDetailDTO;
import com.unchk.AGRT_Backend.dto.ApplicationWithDocumentsDTO;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import com.unchk.AGRT_Backend.exceptions.UserServiceException;
import com.unchk.AGRT_Backend.models.Document;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    // Méthodes principales
    ApplicationDTO createApplicationWithDocuments(ApplicationWithDocumentsDTO applicationWithDocumentsDTO);

    public List<ApplicationDetailDTO> getApplicationsByCurrentUser();

    public ApplicationDetailDTO getApplicationByIdWithDocuments(UUID id);

    public List<ApplicationDetailDTO> getApplicationsByAnnouncementWithDocuments(UUID announcementId);

    ApplicationDetailDTO updateApplication(UUID id, ApplicationWithDocumentsDTO updateDTO);

    ApplicationDTO updateApplicationStatus(UUID id, ApplicationStatus newStatus, String comments);

    void cancelApplication(UUID id) throws UserServiceException;

    // Gestion des documents
    void addDocumentToApplication(UUID applicationId, String base64File, String originalFilename,
            DocumentType documentType);

    void removeDocumentFromApplication(UUID applicationId, UUID documentId);

    List<Document> getApplicationDocuments(UUID applicationId);

    // Validations
    boolean isApplicationComplete(UUID applicationId);

    boolean canCandidateApply(UUID candidateId, UUID announcementId);

    // Méthodes spécifiques
    List<ApplicationDTO> getApplicationsByAcademicYear(UUID academicYearId);

    List<ApplicationDTO> searchApplications(String query);
}