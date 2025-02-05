package com.unchk.AGRT_Backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonIgnore
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName; // Nom original du fichier

    @Column(name = "file_path", nullable = false)
    private String filePath; // Chemin généré pour le stockage

    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
        if (status == null) {
            status = DocumentStatus.VALID;
        }
    }


    public void generateFilePath() {
        if (application == null || application.getId() == null) {
            throw new IllegalStateException("Application must be set before generating file path");
        }

        String announcementId = application.getAnnouncement().getId().toString();
        String applicantId = application.getCandidate().getId().toString();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Assurez-vous que l'ID du document est généré avant
        if (this.getId() == null) {
            // Générer un UUID temporaire si nécessaire
            this.setId(UUID.randomUUID());
        }

        this.filePath = String.format("%s_%s_%s.%s",
                announcementId,
                applicantId,
                this.getId().toString(),
                extension);
    }

    public boolean isValidFileSize() {
        return fileSize != null && fileSize <= MAX_FILE_SIZE;
    }

    public boolean isValidMimeType() {
        return "application/pdf".equals(mimeType);
    }

    public void validate() {
        if (!isValidFileSize()) {
            this.status = DocumentStatus.INVALID;
            throw new IllegalArgumentException("La taille du fichier dépasse la limite maximale de 10MB");
        }
        if (!isValidMimeType()) {
            this.status = DocumentStatus.INVALID;
            throw new IllegalArgumentException("Seuls les fichiers PDF sont acceptés");
        }
        this.status = DocumentStatus.VALID;
    }
}