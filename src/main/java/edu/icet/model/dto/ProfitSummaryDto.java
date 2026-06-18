package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitSummaryDto {
    private double totalRevenue;
    private double totalCost;
    private double profit;
}
