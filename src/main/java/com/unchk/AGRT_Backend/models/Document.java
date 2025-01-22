package com.unchk.AGRT_Backend.models;

import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
        if (status == null) {
            status = DocumentStatus.VALID;
        }
    }

    // Méthodes de validation
    public boolean isValidFileSize() {
        // Par exemple, limite de 10MB
        return fileSize != null && fileSize <= 10 * 1024 * 1024;
    }

    public boolean isValidMimeType() {
        // Types MIME autorisés selon le type de document
        switch (documentType) {
            case CV:
            case MOTIVATION_LETTER:
                return mimeType.equals("application/pdf") || 
                       mimeType.equals("application/msword") ||
                       mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case DIPLOMA:
                return mimeType.equals("application/pdf") ||
                       mimeType.equals("image/jpeg") ||
                       mimeType.equals("image/png");
            case OTHER:
                return mimeType.equals("application/pdf") ||
                       mimeType.startsWith("image/") ||
                       mimeType.equals("application/msword") ||
                       mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default:
                return false;
        }
    }

    // Méthodes utilitaires
    public String getFileExtension() {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public void validate() {
        if (!isValidFileSize() || !isValidMimeType()) {
            this.status = DocumentStatus.INVALID;
        } else {
            this.status = DocumentStatus.VALID;
        }
    }

    // Méthodes de manipulation de fichiers
    public String generateStoragePath() {
        return String.format("documents/%s/%s/%s-%s.%s",
            application.getAcademicYear().getYearName(),
            application.getId(),
            documentType.name().toLowerCase(),
            UUID.randomUUID(),
            getFileExtension());
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isPDF() {
        return "application/pdf".equals(mimeType);
    }

    // Override equals et hashCode pour éviter les problèmes de JPA avec les collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id != null && id.equals(document.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}