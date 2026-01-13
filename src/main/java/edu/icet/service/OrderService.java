package edu.icet.service;

import edu.icet.model.dto.CartItemDto;
import edu.icet.model.dto.CheckoutRequestDto;
import edu.icet.model.dto.InvoiceDto;
import edu.icet.model.dto.OrderDto;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    InvoiceDto placeOrder(List<CartItemDto> cartItems, Integer cashierId, CheckoutRequestDto checkout);
    List<OrderDto> getAllOrders();
    List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId);
    OrderDto getOrderDetails(Integer orderId);
}
