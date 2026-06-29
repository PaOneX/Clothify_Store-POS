package edu.icet.service.Impl;

import edu.icet.model.dto.InventoryLogDto;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.InventoryReason;
import edu.icet.repository.InventoryRepository;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.service.InventoryService;
import edu.icet.util.CrudUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(ProductVariantRepository variantRepository, InventoryRepository inventoryRepository) {
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public ObservableList<ProductVariantDto> getInventoryVariants() {
        return FXCollections.observableArrayList(variantRepository.findAllActive());
    }

    @Override
    public void adjustStock(Integer variantId, int changeQty, InventoryReason reason, Integer userId) {
        ProductVariantDto variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        String displayName = variant.getDisplayName();

        CrudUtil.executeInTransaction(connection -> {
            variantRepository.adjustStock(connection, variantId, changeQty, displayName);
            inventoryRepository.logChange(connection, variantId, changeQty, reason, userId);
            return null;
        });
    }

    @Override
    public ObservableList<InventoryLogDto> getLogs() {
        return FXCollections.observableArrayList(inventoryRepository.getLogs());
    }
}
