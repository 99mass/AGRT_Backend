package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.dto.ApplicationDTO;
import com.unchk.AGRT_Backend.dto.ApplicationWithDocumentsDTO;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import com.unchk.AGRT_Backend.models.Document;


import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    // Méthodes principales
    ApplicationDTO createApplicationWithDocuments(ApplicationWithDocumentsDTO applicationWithDocumentsDTO);
    ApplicationDTO getApplicationById(UUID id);
    List<ApplicationDTO> getAllApplications();
    List<ApplicationDTO> getApplicationsByCandidate(UUID candidateId);
    List<ApplicationDTO> getApplicationsByAnnouncement(UUID announcementId);
    ApplicationDTO updateApplicationStatus(UUID id, ApplicationStatus newStatus, String comments);
    
    // Gestion des documents
    void addDocumentToApplication(UUID applicationId, String base64File, String originalFilename, DocumentType documentType);
    void removeDocumentFromApplication(UUID applicationId, UUID documentId);
    List<Document> getApplicationDocuments(UUID applicationId);
    
    // Validations
    boolean isApplicationComplete(UUID applicationId);
    boolean canCandidateApply(UUID candidateId, UUID announcementId);
    
    // Méthodes spécifiques
    List<ApplicationDTO> getApplicationsByAcademicYear(UUID academicYearId);
    List<ApplicationDTO> searchApplications(String query);
}