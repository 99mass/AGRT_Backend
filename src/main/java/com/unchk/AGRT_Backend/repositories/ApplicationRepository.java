package com.unchk.AGRT_Backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unchk.AGRT_Backend.models.Application;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;
import com.unchk.AGRT_Backend.enums.ApplicationType;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    // Rechercher par candidat
    List<Application> findByCandidateId(UUID candidateId);
    
    // Rechercher par annonce
    List<Application> findByAnnouncementId(UUID announcementId);
    
    // Rechercher par année académique
    List<Application> findByAcademicYearId(UUID academicYearId);
    
    // Rechercher par statut
    List<Application> findByStatus(ApplicationStatus status);
    
    // Rechercher par type d'application
    List<Application> findByApplicationType(ApplicationType applicationType);
    
    // Vérifier si une candidature existe déjà pour un candidat et une annonce
    boolean existsByCandidateIdAndAnnouncementId(UUID candidateId, UUID announcementId);
    
    // Rechercher les candidatures récentes
    List<Application> findByCreatedAtAfter(LocalDateTime date);
    
    // Rechercher les candidatures par statut et type
    List<Application> findByStatusAndApplicationType(ApplicationStatus status, ApplicationType type);
    
    // Recherche personnalisée pour les candidatures complètes
    @Query("SELECT a FROM Application a WHERE a.status = :status AND SIZE(a.documents) >= 2")
    List<Application> findCompleteApplicationsByStatus(@Param("status") ApplicationStatus status);
    
    // Compter les candidatures par statut pour une annonce spécifique
    @Query("SELECT COUNT(a) FROM Application a WHERE a.announcement.id = :announcementId AND a.status = :status")
    Long countByAnnouncementAndStatus(@Param("announcementId") UUID announcementId, @Param("status") ApplicationStatus status);
}