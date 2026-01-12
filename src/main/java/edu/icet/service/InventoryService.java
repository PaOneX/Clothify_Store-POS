package edu.icet.service;

import edu.icet.model.dto.InventoryLogDto;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.InventoryReason;
import javafx.collections.ObservableList;

public interface InventoryService {
    ObservableList<ProductVariantDto> getInventoryVariants();
    void adjustStock(Integer variantId, int changeQty, InventoryReason reason, Integer userId);
    ObservableList<InventoryLogDto> getLogs();
}
