package com.unchk.AGRT_Backend.models;

import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.ApplicationType;
import com.unchk.AGRT_Backend.enums.DocumentStatus;
import com.unchk.AGRT_Backend.enums.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "candidate_id", "announcement_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private JobAnnouncement announcement;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApplicationHistory> history = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setApplication(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setApplication(null);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setApplication(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setApplication(null);
    }

    public void updateStatus(ApplicationStatus newStatus, User changedBy, String comments) {
        if (this.status == newStatus) {
            return;
        }

        ApplicationHistory historyEntry = new ApplicationHistory();
        historyEntry.setApplication(this);
        historyEntry.setStatusFrom(this.status);
        historyEntry.setStatusTo(newStatus);
        historyEntry.setChangedBy(changedBy);
        historyEntry.setComments(comments);
        historyEntry.setChangeDate(LocalDateTime.now());

        this.status = newStatus;
        this.history.add(historyEntry);

        if (newStatus == ApplicationStatus.REJECTED) {
            this.rejectionReason = comments;
        }
    }

    public boolean canBeUpdated() {
        return status == ApplicationStatus.PENDING ||
                status == ApplicationStatus.UNDER_REVIEW;
    }

    public boolean isComplete() {
        return documents.stream()
                .anyMatch(doc -> doc.getDocumentType().equals(DocumentType.CV)) &&
                documents.stream()
                        .anyMatch(doc -> doc.getDocumentType().equals(DocumentType.MOTIVATION_LETTER));
    }

    public boolean validateDocuments() {
        return documents.stream()
                .allMatch(doc -> doc.getStatus() == DocumentStatus.VALID);
    }
}