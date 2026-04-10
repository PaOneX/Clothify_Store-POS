package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.ClothingSize;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.service.ProductVariantService;
import edu.icet.util.BarcodeUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;

    public ProductVariantServiceImpl(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Override
    public int addVariant(ProductVariantDto variant) {
        validateDuplicate(variant.getProductId(), variant.getSize(), variant.getColor(), null);
        return variantRepository.addVariant(variant);
    }

    @Override
    public void updateVariant(ProductVariantDto variant) {
        validateDuplicate(variant.getProductId(), variant.getSize(), variant.getColor(), variant.getVariantId());
        variantRepository.updateVariant(variant);
    }

    @Override
    public void deleteVariant(Integer variantId) {
        variantRepository.deleteVariant(variantId);
    }

    @Override
    public List<ProductVariantDto> getByProductId(Integer productId) {
        return variantRepository.findByProductId(productId);
    }

    @Override
    public ObservableList<ProductVariantDto> getAllActiveVariants() {
        return FXCollections.observableArrayList(variantRepository.findAllActive());
    }

    @Override
    public ObservableList<ProductVariantDto> searchVariants(String term, Integer categoryId, Double minPrice, Double maxPrice) {
        return FXCollections.observableArrayList(variantRepository.search(term, categoryId, minPrice, maxPrice));
    }

    @Override
    public ProductVariantDto findByBarcode(String barcode) {
        return variantRepository.findByBarcode(barcode).orElse(null);
    }

    @Override
    public String generateBarcode(Integer variantId) {
        ProductVariantDto variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        String barcode = BarcodeUtil.generateEan13(variantId);
        variantRepository.updateBarcode(variantId, barcode);
        return barcode;
    }

    @Override
    public List<ProductVariantDto> getLowStockVariants() {
        return variantRepository.findLowStock(AppConfig.getLowStockThreshold());
    }

    @Override
    public int addVariantsForSizes(Integer productId, String skuPrefix, String color, double price, int qtyPerSize, List<ClothingSize> sizes) {
        if (productId == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (sizes == null || sizes.isEmpty()) {
            throw new IllegalArgumentException("Select at least one size");
        }
        String prefix = skuPrefix == null ? "" : skuPrefix.trim();
        String normalizedColor = color == null ? "" : color.trim();
        int created = 0;
        for (ClothingSize size : sizes) {
            String sizeValue = size.name();
            if (variantRepository.existsDuplicate(productId, sizeValue, normalizedColor, null)) {
                continue;
            }
            ProductVariantDto dto = new ProductVariantDto();
            dto.setProductId(productId);
            dto.setSku(prefix.isEmpty() ? sizeValue : prefix + "-" + sizeValue);
            dto.setSize(sizeValue);
            dto.setColor(normalizedColor);
            dto.setPrice(price);
            dto.setQtyOnHand(qtyPerSize);
            dto.setActive(true);
            variantRepository.addVariant(dto);
            created++;
        }
        if (created == 0) {
            throw new IllegalArgumentException("All selected sizes already exist for this color");
        }
        return created;
    }

    private void validateDuplicate(Integer productId, String size, String color, Integer excludeVariantId) {
        if (variantRepository.existsDuplicate(productId, size, color, excludeVariantId)) {
            throw new IllegalArgumentException("A variant with this size and color already exists for the product");
        }
    }
}
