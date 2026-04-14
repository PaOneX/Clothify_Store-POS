package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlowMovingDto {
    private Integer variantId;
    private String productName;
    private String size;
    private Integer qtyOnHand;
    private int daysSinceLastSale;
}
