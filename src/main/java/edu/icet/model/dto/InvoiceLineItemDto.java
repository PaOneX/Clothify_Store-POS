package edu.icet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceLineItemDto {
    private String productName;
    private Integer qty;
    private Double unitPrice;
    private Double lineTotal;
}
