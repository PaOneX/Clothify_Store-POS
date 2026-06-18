package edu.icet.model.dto;

import edu.icet.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequestDto {
    private Integer customerId;
    private Integer discountId;
    private String discountCode;
    private Double manualDiscountPercent;
    private Double manualDiscountFixed;
    private PaymentMethod paymentMethod;
    private Double amountReceived;
    private List<OrderPaymentDto> splitPayments = new ArrayList<>();
}
