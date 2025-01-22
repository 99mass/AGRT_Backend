package com.unchk.AGRT_Backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.unchk.AGRT_Backend.models.Document;
import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    // Rechercher par application
    List<Document> findByApplicationId(UUID applicationId);
    
    // Rechercher par type de document
    List<Document> findByDocumentType(DocumentType documentType);
    
    // Rechercher par statut
    List<Document> findByStatus(DocumentStatus status);
    
    // Rechercher par application et type de document
    List<Document> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
    
    // Vérifier si un document existe pour une application
    boolean existsByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
    
    // Rechercher par type MIME
    List<Document> findByMimeType(String mimeType);
    
    // Compter les documents par application
    long countByApplicationId(UUID applicationId);
}