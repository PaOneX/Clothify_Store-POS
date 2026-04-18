package edu.icet.service;

import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.ClothingSize;
import javafx.collections.ObservableList;

import java.util.List;

public interface ProductVariantService {
    int addVariant(ProductVariantDto variant);
    void updateVariant(ProductVariantDto variant);
    void deleteVariant(Integer variantId);
    List<ProductVariantDto> getByProductId(Integer productId);
    ObservableList<ProductVariantDto> getAllActiveVariants();
    ObservableList<ProductVariantDto> searchVariants(String term, Integer categoryId, Double minPrice, Double maxPrice);
    ProductVariantDto findByBarcode(String barcode);
    String generateBarcode(Integer variantId);
    List<ProductVariantDto> getLowStockVariants();
    int addVariantsForSizes(Integer productId, String skuPrefix, String color, double price, int qtyPerSize, List<ClothingSize> sizes);
}
