package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductDto {
    private Integer productId;
    private String productName;
    private int totalQtySold;
    private double totalRevenue;
}
