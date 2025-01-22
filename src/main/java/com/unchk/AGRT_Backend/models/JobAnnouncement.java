package com.unchk.AGRT_Backend.models;

import com.unchk.AGRT_Backend.enums.AnnouncementStatus;
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

@Entity
@Table(name = "job_announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementStatus status;

    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    @Column(name = "closing_date", nullable = false)
    private LocalDateTime closingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Bidirectional relationship with Applications
    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private Set<Application> applications = new HashSet<>();

    // Helper methods to maintain bidirectional relationship
    public void addApplication(Application application) {
        applications.add(application);
        application.setAnnouncement(this);
    }

    public void removeApplication(Application application) {
        applications.remove(application);
        application.setAnnouncement(null);
    }

    // Business logic methods
    public boolean isOpen() {
        LocalDateTime now = LocalDateTime.now();
        return status == AnnouncementStatus.PUBLISHED &&
                publicationDate != null &&
                publicationDate.isBefore(now) &&
                closingDate.isAfter(now);
    }

    public boolean canBePublished() {
        return status == AnnouncementStatus.DRAFT &&
                title != null &&
                !title.trim().isEmpty() &&
                description != null &&
                !description.trim().isEmpty() &&
                closingDate != null &&
                closingDate.isAfter(LocalDateTime.now());
    }

    public void publish() {
        if (!canBePublished()) {
            throw new IllegalStateException("Announcement cannot be published");
        }
        this.status = AnnouncementStatus.PUBLISHED;
        this.publicationDate = LocalDateTime.now();
    }

    public void close() {
        if (status != AnnouncementStatus.PUBLISHED) {
            throw new IllegalStateException("Only published announcements can be closed");
        }
        this.status = AnnouncementStatus.CLOSED;
    }
}