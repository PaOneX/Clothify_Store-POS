package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.NotificationDto;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.repository.NotificationRepository;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.service.NotificationService;

import java.util.List;

public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProductVariantRepository variantRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   ProductVariantRepository variantRepository) {
        this.notificationRepository = notificationRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public List<NotificationDto> getActiveNotifications() {
        return notificationRepository.findActive();
    }

    @Override
    public void checkAndCreateAlerts() {
        int threshold = AppConfig.getLowStockThreshold();
        List<ProductVariantDto> lowStock = variantRepository.findLowStock(threshold);
        for (ProductVariantDto v : lowStock) {
            String msg = String.format("Low stock: %s — only %d left", v.getDisplayName(), v.getQtyOnHand());
            notificationRepository.save("LOW_STOCK", msg);
        }
    }

    @Override
    public void dismiss(Integer notificationId) {
        notificationRepository.dismiss(notificationId);
    }
}
