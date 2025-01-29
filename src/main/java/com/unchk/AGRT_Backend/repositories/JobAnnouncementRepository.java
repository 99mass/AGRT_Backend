package com.unchk.AGRT_Backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.unchk.AGRT_Backend.models.JobAnnouncement;
import com.unchk.AGRT_Backend.enums.AnnouncementStatus;

@Repository
public interface JobAnnouncementRepository extends JpaRepository<JobAnnouncement, UUID> {
    // Rechercher par status
    List<JobAnnouncement> findByStatus(AnnouncementStatus status);
    
    // Rechercher par année académique
    List<JobAnnouncement> findByAcademicYearId(UUID academicYearId);
    
    // Rechercher les annonces ouvertes
    List<JobAnnouncement> findByStatusAndClosingDateAfter(AnnouncementStatus status, LocalDateTime date);
    
    // Rechercher par créateur
    List<JobAnnouncement> findByCreatedById(UUID createdById);
    
    // Rechercher les annonces actives
    @Query("SELECT j FROM JobAnnouncement j WHERE j.status = 'PUBLISHED' AND j.publicationDate <= CURRENT_TIMESTAMP AND j.closingDate > CURRENT_TIMESTAMP")
    List<JobAnnouncement> findActiveAnnouncements();
    
    // Compter les annonces par statut
    long countByStatus(AnnouncementStatus status);
    
    // Rechercher par titre
    List<JobAnnouncement> findByTitleContainingIgnoreCase(String title);
    
    // Rechercher les annonces qui expirent bientôt
    @Query("SELECT j FROM JobAnnouncement j WHERE j.status = 'PUBLISHED' AND j.closingDate BETWEEN CURRENT_TIMESTAMP AND :endDate")
    List<JobAnnouncement> findExpiringAnnouncements(LocalDateTime endDate);

    boolean existsByTitleAndAcademicYear_Id(String title, UUID academicYearId);
}