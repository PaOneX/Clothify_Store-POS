package edu.icet.service;

import edu.icet.model.dto.DiscountDto;
import javafx.collections.ObservableList;

public interface DiscountService {
    int save(DiscountDto discount);
    void update(DiscountDto discount);
    void delete(Integer discountId);
    ObservableList<DiscountDto> getAll();
    DiscountDto findById(Integer discountId);
    double calculateDiscount(DiscountDto discount, double subtotal);
    DiscountDto validateCode(String code, double subtotal);
    double calculateManualDiscount(Double percent, Double fixed, double subtotal);
}
