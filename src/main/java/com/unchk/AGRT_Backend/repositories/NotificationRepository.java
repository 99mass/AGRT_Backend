package com.unchk.AGRT_Backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.unchk.AGRT_Backend.models.Notification;
import com.unchk.AGRT_Backend.enums.NotificationStatus;
import com.unchk.AGRT_Backend.enums.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // Rechercher par utilisateur
    List<Notification> findByUserId(UUID userId);
    
    // Rechercher par application
    List<Notification> findByApplicationId(UUID applicationId);
    
    // Rechercher par statut
    List<Notification> findByStatus(NotificationStatus status);
    
    // Rechercher par type
    List<Notification> findByType(NotificationType type);
    
    // Rechercher les notifications non lues par utilisateur
    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);
    
    // Rechercher les notifications récentes
    List<Notification> findBySentAtAfter(LocalDateTime date);
    
    // Compter les notifications non lues par utilisateur
    long countByUserIdAndStatus(UUID userId, NotificationStatus status);
    
    // Rechercher par utilisateur et type
    List<Notification> findByUserIdAndType(UUID userId, NotificationType type);
}