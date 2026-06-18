package edu.icet.repository;

import edu.icet.model.dto.ProductVariantDto;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository {
    int addVariant(ProductVariantDto variant);
    void updateVariant(ProductVariantDto variant);
    void deleteVariant(Integer variantId);
    List<ProductVariantDto> findByProductId(Integer productId);
    List<ProductVariantDto> findAllActive();
    List<ProductVariantDto> search(String term, Integer categoryId, Double minPrice, Double maxPrice);
    Optional<ProductVariantDto> findById(Integer variantId);
    Optional<ProductVariantDto> findByBarcode(String barcode);
    void deductStock(Connection connection, Integer variantId, int qty, String displayName);
    void adjustStock(Connection connection, Integer variantId, int changeQty, String displayName);
    int getQuantity(Integer variantId);
    void updateBarcode(Integer variantId, String barcode);
    List<ProductVariantDto> findLowStock(int threshold);
}
