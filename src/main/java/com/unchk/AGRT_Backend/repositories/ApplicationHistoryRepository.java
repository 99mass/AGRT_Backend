package com.unchk.AGRT_Backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.unchk.AGRT_Backend.models.ApplicationHistory;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, UUID> {
    // Rechercher par application
    List<ApplicationHistory> findByApplicationId(UUID applicationId);
    
    // Rechercher par utilisateur qui a fait le changement
    List<ApplicationHistory> findByChangedById(UUID changedById);
    
    // Rechercher par statut de départ
    List<ApplicationHistory> findByStatusFrom(ApplicationStatus statusFrom);
    
    // Rechercher par statut d'arrivée
    List<ApplicationHistory> findByStatusTo(ApplicationStatus statusTo);
    
    // Rechercher les changements récents
    List<ApplicationHistory> findByChangeDateAfter(LocalDateTime date);
    
    // Rechercher par application et statut final
    List<ApplicationHistory> findByApplicationIdAndStatusTo(UUID applicationId, ApplicationStatus statusTo);
    
    // Compter les changements par application
    long countByApplicationId(UUID applicationId);
}