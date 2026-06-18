package edu.icet.service;

import edu.icet.model.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getActiveNotifications();
    void checkAndCreateAlerts();
    void dismiss(Integer notificationId);
}
