package edu.icet.model.dto;

import edu.icet.model.enums.OrderStatus;
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
public class OrderDto {
    private Integer orderId;
    private LocalDateTime orderDate;
    private Integer cashierId;
    private String cashierName;
    private Integer customerId;
    private String customerName;
    private Double subtotal;
    private Double discountAmount;
    private Integer discountId;
    private Double tax;
    private Double total;
    private PaymentMethod paymentMethod;
    private Double amountReceived;
    private Double changeGiven;
    private OrderStatus status;
    private List<OrderItemDto> items = new ArrayList<>();
    private List<OrderPaymentDto> payments = new ArrayList<>();
}
