package edu.icet.repository;

import edu.icet.model.dto.OrderDto;
import edu.icet.model.dto.OrderItemDto;
import edu.icet.model.dto.OrderPaymentDto;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    int createOrder(OrderDto order);
    int createOrder(Connection connection, OrderDto order);
    void addOrderItem(OrderItemDto item);
    void addOrderItem(Connection connection, OrderItemDto item);
    void addOrderPayment(Connection connection, OrderPaymentDto payment);
    List<OrderDto> findAll();
    List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId);
    List<OrderDto> findByCustomerId(Integer customerId);
    Optional<OrderDto> findById(Integer orderId);
    Optional<OrderDto> findByIdAndCustomerPhone(Integer orderId, String phone);
    Optional<OrderDto> findByInvoiceNo(String invoiceNo);
    List<OrderItemDto> findItemsByOrderId(Integer orderId);
    List<OrderPaymentDto> findPaymentsByOrderId(Integer orderId);
}
