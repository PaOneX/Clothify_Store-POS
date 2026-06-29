package edu.icet.model.dto;

import edu.icet.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentDto {
    private Integer paymentId;
    private Integer orderId;
    private PaymentMethod method;
    private Double amount;
}
