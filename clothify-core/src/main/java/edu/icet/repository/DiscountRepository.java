package edu.icet.repository;

import edu.icet.model.dto.DiscountDto;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository {
    int save(DiscountDto discount);
    void update(DiscountDto discount);
    void delete(Integer discountId);
    List<DiscountDto> findAll();
    Optional<DiscountDto> findById(Integer discountId);
    Optional<DiscountDto> findByCode(String code);
}
