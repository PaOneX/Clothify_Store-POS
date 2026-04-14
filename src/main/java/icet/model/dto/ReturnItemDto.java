package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnItemDto {
    private Integer returnItemId;
    private Integer returnId;
    private Integer variantId;
    private String productName;
    private String size;
    private String color;
    private Integer qty;
    private Double unitPrice;
}
