package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Integer variantId;
    private Integer productId;
    private String productName;
    private String size;
    private String color;
    private Integer qty;
    private Double unitPrice;

    public Double getLineTotal() {
        return unitPrice * qty;
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(productName != null ? productName : "");
        if (size != null && !size.isBlank()) {
            sb.append(" (").append(size);
            if (color != null && !color.isBlank()) {
                sb.append(" / ").append(color);
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
