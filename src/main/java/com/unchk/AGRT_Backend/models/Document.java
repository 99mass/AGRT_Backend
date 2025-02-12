package com.unchk.AGRT_Backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonBackReference
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Integer fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (uploadDate == null) {
            uploadDate = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = DocumentStatus.VALID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void generateFilePath() {
        if (application == null || application.getId() == null) {
            throw new IllegalStateException("Application must be set before generating file path");
        }

        String announcementId = application.getAnnouncement().getId().toString();
        String applicantId = application.getCandidate().getId().toString();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        if (this.getId() == null) {
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