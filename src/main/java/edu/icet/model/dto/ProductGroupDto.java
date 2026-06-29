package edu.icet.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class ProductGroupDto {
    private Integer productId;
    private String productName;
    private String imagePath;
    private List<ProductVariantDto> variants = new ArrayList<>();

    public double getMinPrice() {
        return variants.stream().mapToDouble(ProductVariantDto::getPrice).min().orElse(0);
    }

    public double getMaxPrice() {
        return variants.stream().mapToDouble(ProductVariantDto::getPrice).max().orElse(0);
    }

    public int getTotalStock() {
        return variants.stream().mapToInt(v -> v.getQtyOnHand() != null ? v.getQtyOnHand() : 0).sum();
    }

    public boolean isFullyOutOfStock() {
        return variants.stream().noneMatch(v -> v.getQtyOnHand() != null && v.getQtyOnHand() > 0);
    }

    public boolean hasMultipleVariants() {
        return variants.size() > 1;
    }

    public String getPriceLabel() {
        double min = getMinPrice();
        double max = getMaxPrice();
        if (variants.size() <= 1 || min == max) {
            return String.format("Rs. %.2f", min);
        }
        return String.format("Rs. %.2f – %.2f", min, max);
    }

    public ProductVariantDto getFirstInStockVariant() {
        return variants.stream()
                .filter(v -> v.getQtyOnHand() != null && v.getQtyOnHand() > 0)
                .findFirst()
                .orElse(variants.isEmpty() ? null : variants.get(0));
    }

    public static ProductGroupDto fromVariants(Integer productId, List<ProductVariantDto> variants) {
        ProductGroupDto group = new ProductGroupDto();
        group.setProductId(productId);
        if (!variants.isEmpty()) {
            ProductVariantDto first = variants.get(0);
            group.setProductName(first.getProductName());
            group.setImagePath(first.getImagePath());
        }
        variants.stream()
                .sorted(Comparator.comparing(v -> v.getSize() != null ? v.getSize() : ""))
                .forEach(group.getVariants()::add);
        return group;
    }
}
