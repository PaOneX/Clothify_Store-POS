package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesSummaryDto {
    private int orderCount;
    private double totalRevenue;
    private double totalTax;
}
