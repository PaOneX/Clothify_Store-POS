package edu.icet.repository;

import edu.icet.model.dto.NotificationDto;

import java.util.List;

public interface NotificationRepository {
    void save(String type, String message);
    List<NotificationDto> findActive();
    void dismiss(Integer notificationId);
}
