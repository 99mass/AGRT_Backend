package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.models.Application;
import com.unchk.AGRT_Backend.models.Notification;
import com.unchk.AGRT_Backend.enums.NotificationType;

public interface NotificationService {
    void sendApplicationCreatedNotification(Application application);
    void sendStatusUpdateNotification(Application application);
    void sendDocumentRequestNotification(Application application);
    Notification createNotification(Application application, NotificationType type, String message);
}