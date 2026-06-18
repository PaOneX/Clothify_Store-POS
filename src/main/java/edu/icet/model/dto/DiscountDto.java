package edu.icet.model.dto;

import edu.icet.model.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDto {
    private Integer discountId;
    private String code;
    private String name;
    private DiscountType type;
    private Double value;
    private Double minOrder;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean active;
}
