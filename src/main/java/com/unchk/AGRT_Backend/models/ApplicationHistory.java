package com.unchk.AGRT_Backend.models;

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

import com.unchk.AGRT_Backend.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_from", nullable = false)
    private ApplicationStatus statusFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_to", nullable = false)
    private ApplicationStatus statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @PrePersist
    protected void onCreate() {
        if (changeDate == null) {
            changeDate = LocalDateTime.now();
        }
    }

    // Méthodes utilitaires
    public String getChangeDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Status changed from ")
                .append(statusFrom)
                .append(" to ")
                .append(statusTo)
                .append(" by ")
                .append(changedBy.getFirstName())
                .append(" ")
                .append(changedBy.getLastName());

        if (comments != null && !comments.trim().isEmpty()) {
            description.append(". Comments: ").append(comments);
        }

        return description.toString();
    }

    public boolean isRejection() {
        return statusTo == ApplicationStatus.REJECTED;
    }

    public boolean isAcceptance() {
        return statusTo == ApplicationStatus.ACCEPTED;
    }

    public boolean isUnderReview() {
        return statusTo == ApplicationStatus.UNDER_REVIEW;
    }

    public long getDurationSinceChange() {
        return java.time.Duration.between(changeDate, LocalDateTime.now()).toHours();
    }
}
