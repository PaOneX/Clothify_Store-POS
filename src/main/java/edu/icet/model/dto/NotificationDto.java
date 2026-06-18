package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Integer notificationId;
    private String type;
    private String message;
    private Boolean dismissed;
    private LocalDateTime createdAt;
}
