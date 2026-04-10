package edu.icet.service.Impl;

import edu.icet.model.dto.ReturnDto;
import edu.icet.model.dto.ReturnItemDto;
import edu.icet.model.enums.InventoryReason;
import edu.icet.repository.InventoryRepository;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.repository.ReturnRepository;
import edu.icet.service.ReturnService;
import edu.icet.util.CrudUtil;

import java.util.List;

public class ReturnServiceImpl implements ReturnService {

    private final ReturnRepository returnRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    public ReturnServiceImpl(ReturnRepository returnRepository,
                             ProductVariantRepository variantRepository,
                             InventoryRepository inventoryRepository) {
        this.returnRepository = returnRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void processReturn(ReturnDto returnDto, List<ReturnItemDto> items, Integer cashierId) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No items to return");
        }
        returnDto.setCashierId(cashierId);
        double refund = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQty()).sum();
        returnDto.setRefundAmount(refund);

        CrudUtil.executeInTransaction(connection -> {
            int returnId = returnRepository.createReturn(connection, returnDto);
            for (ReturnItemDto item : items) {
                item.setReturnId(returnId);
                returnRepository.addReturnItem(connection, item);
                variantRepository.adjustStock(connection, item.getVariantId(), item.getQty(), item.getProductName());
                inventoryRepository.logChange(connection, item.getVariantId(), item.getQty(), InventoryReason.RETURN, cashierId);
            }
            return null;
        });
    }

    @Override
    public List<ReturnDto> getAllReturns() {
        return returnRepository.findAll();
    }
}
