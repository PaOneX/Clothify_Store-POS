package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryLogDto {
    private Integer logId;
    private Integer variantId;
    private String productName;
    private String size;
    private String color;
    private Integer changeQty;
    private String reason;
    private Integer userId;
    private String username;
    private LocalDateTime createdAt;
}
