package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineOrderResponseDto {
    private Integer orderId;
    private String status;
    private Double total;
    private String message;
}
