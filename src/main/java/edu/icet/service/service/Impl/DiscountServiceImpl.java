package edu.icet.service.Impl;

import edu.icet.model.dto.DiscountDto;
import edu.icet.model.enums.DiscountType;
import edu.icet.repository.DiscountRepository;
import edu.icet.service.DiscountService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public int save(DiscountDto discount) {
        if (discount.getCode() != null) {
            discount.setCode(discount.getCode().trim().toUpperCase());
        }
        return discountRepository.save(discount);
    }

    @Override
    public void update(DiscountDto discount) {
        if (discount.getCode() != null) {
            discount.setCode(discount.getCode().trim().toUpperCase());
        }
        discountRepository.update(discount);
    }

    @Override
    public void delete(Integer discountId) {
        discountRepository.delete(discountId);
    }

    @Override
    public ObservableList<DiscountDto> getAll() {
        return FXCollections.observableArrayList(discountRepository.findAll());
    }

    @Override
    public DiscountDto findById(Integer discountId) {
        return discountRepository.findById(discountId).orElse(null);
    }

    @Override
    public double calculateDiscount(DiscountDto discount, double subtotal) {
        if (discount == null || !Boolean.TRUE.equals(discount.getActive())) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (discount.getValidFrom() != null && today.isBefore(discount.getValidFrom())) {
            return 0;
        }
        if (discount.getValidTo() != null && today.isAfter(discount.getValidTo())) {
            return 0;
        }
        if (discount.getMinOrder() != null && subtotal < discount.getMinOrder()) {
            return 0;
        }
        if (discount.getType() == DiscountType.PERCENTAGE) {
            return Math.min(subtotal, subtotal * discount.getValue() / 100.0);
        }
        return Math.min(subtotal, discount.getValue());
    }

    @Override
    public DiscountDto validateCode(String code, double subtotal) {
        DiscountDto discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));
        double amount = calculateDiscount(discount, subtotal);
        if (amount <= 0) {
            throw new IllegalArgumentException("Coupon not applicable to this order");
        }
        return discount;
    }

    @Override
    public double calculateManualDiscount(Double percent, Double fixed, double subtotal) {
        double discount = 0;
        if (percent != null && percent > 0) {
            discount = subtotal * percent / 100.0;
        }
        if (fixed != null && fixed > 0) {
            discount = Math.max(discount, fixed);
        }
        return Math.min(subtotal, discount);
    }
}
