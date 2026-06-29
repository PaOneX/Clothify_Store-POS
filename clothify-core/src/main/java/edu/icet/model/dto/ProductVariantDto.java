package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDto {
    private Integer variantId;
    private Integer productId;
    private String sku;
    private String size;
    private String color;
    private String barcode;
    private Double price;
    private Double costPrice;
    private Integer qtyOnHand;
    private Boolean active;
    // Joined from product
    private String productName;
    private String description;
    private String imagePath;
    private Integer categoryId;
    private Integer supplierId;
    private String categoryName;
    private String supplierName;

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(productName != null ? productName : "");
        if (size != null && !size.isBlank()) {
            sb.append(" (").append(size);
            if (color != null && !color.isBlank()) {
                sb.append(" / ").append(color);
            }
            sb.append(")");
        } else if (color != null && !color.isBlank()) {
            sb.append(" (").append(color).append(")");
        }
        return sb.toString();
    }
}
