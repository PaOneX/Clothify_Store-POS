package edu.icet.service.Impl;

import edu.icet.model.dto.DiscountDto;
import edu.icet.model.enums.DiscountType;
import edu.icet.repository.DiscountRepository;
import edu.icet.service.DiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DiscountServiceImplTest {

    private DiscountRepository repository;
    private DiscountService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DiscountRepository.class);
        service = new DiscountServiceImpl(repository);
    }

    @Test
    void calculatePercentageDiscount() {
        DiscountDto discount = new DiscountDto(1, "TEST10", "Test", DiscountType.PERCENTAGE, 10.0,
                0.0, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), true);
        assertEquals(100.0, service.calculateDiscount(discount, 1000.0), 0.01);
    }

    @Test
    void calculateFixedDiscountCappedAtSubtotal() {
        DiscountDto discount = new DiscountDto(1, "FLAT", "Flat", DiscountType.FIXED, 500.0,
                0.0, null, null, true);
        assertEquals(300.0, service.calculateDiscount(discount, 300.0), 0.01);
    }

    @Test
    void validateCodeThrowsForInvalid() {
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.validateCode("BAD", 1000));
    }
}
