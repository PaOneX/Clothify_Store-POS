package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventorySummaryDto {
    private int totalSkus;
    private int totalUnits;
    private int lowStockCount;
}
