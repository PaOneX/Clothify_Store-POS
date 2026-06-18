package edu.icet.model.dto;

import edu.icet.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDto {
    private Integer invoiceId;
    private Integer orderId;
    private String invoiceNo;
    private LocalDateTime generatedAt;
    private String cashierName;
    private String customerName;
    private Double subtotal;
    private Double discountAmount;
    private Double tax;
    private Double total;
    private PaymentMethod paymentMethod;
    private String paymentDetails;
    private List<OrderItemDto> items = new ArrayList<>();
}
