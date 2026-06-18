package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.ProductVariantDto;
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
        return variantRepository.addVariant(variant);
    }

    @Override
    public void updateVariant(ProductVariantDto variant) {
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
}
