package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCatalogDto {
    private Integer id;
    private String productName;
    private String description;
    private String imagePath;
    private String imageUrl;
    private Integer categoryId;
    private String categoryName;
    private Double minPrice;
    private Double maxPrice;
    private Integer totalQty;
    private List<ProductVariantDto> variants = new ArrayList<>();
}
