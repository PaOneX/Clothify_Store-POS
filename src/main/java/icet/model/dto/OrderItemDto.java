package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private Integer orderItemId;
    private Integer orderId;
    private Integer variantId;
    private Integer productId;
    private String productName;
    private String size;
    private String color;
    private Integer qty;
    private Double unitPrice;
    private Double lineTotal;
}
