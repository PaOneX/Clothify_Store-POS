package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Integer id;
    private String productName;
    private String description;
    private String imagePath;
    private Integer categoryId;
    private Integer supplierId;
    private String categoryName;
    private String supplierName;
    // Aggregated from variants for display
    private Double minPrice;
    private Double maxPrice;
    private Integer totalQty;
    private Integer variantCount;
}
