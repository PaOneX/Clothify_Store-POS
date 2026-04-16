package edu.icet.repository;

import edu.icet.model.dto.InventoryLogDto;
import edu.icet.model.enums.InventoryReason;

import java.sql.Connection;
import java.util.List;

public interface InventoryRepository {
    void logChange(Integer variantId, int changeQty, InventoryReason reason, Integer userId);
    void logChange(Connection connection, Integer variantId, int changeQty, InventoryReason reason, Integer userId);
    List<InventoryLogDto> getLogs();
    List<InventoryLogDto> getLogsByVariant(Integer variantId);
}
