package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.*;
import edu.icet.model.enums.InventoryReason;
import edu.icet.model.enums.OrderSource;
import edu.icet.model.enums.OrderStatus;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.repository.InventoryRepository;
import edu.icet.repository.InvoiceRepository;
import edu.icet.repository.OrderRepository;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.service.DiscountService;
import edu.icet.service.OrderService;
import edu.icet.util.CrudUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final InvoiceRepository invoiceRepository;
    private final DiscountService discountService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductVariantRepository variantRepository,
                            InventoryRepository inventoryRepository,
                            InvoiceRepository invoiceRepository,
                            DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.invoiceRepository = invoiceRepository;
        this.discountService = discountService;
    }

    @Override
    public InvoiceDto placeOrder(List<CartItemDto> cartItems, Integer cashierId, CheckoutRequestDto checkout) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        if (checkout == null) {
            checkout = new CheckoutRequestDto();
            checkout.setPaymentMethod(PaymentMethod.CASH);
        }
        if (checkout.getOrderSource() == null) {
            checkout.setOrderSource(OrderSource.POS);
        }
        final CheckoutRequestDto checkoutRequest = checkout;

        double subtotal = cartItems.stream().mapToDouble(CartItemDto::getLineTotal).sum();
        double discountAmount = resolveDiscount(checkoutRequest, subtotal);
        double taxable = subtotal - discountAmount;
        double tax = taxable * AppConfig.getTaxRate();
        double total = taxable + tax;

        boolean onlineOrder = checkoutRequest.getOrderSource() == OrderSource.ONLINE;
        if (!onlineOrder) {
            validatePayment(checkoutRequest, total);
        } else if (checkoutRequest.getPaymentMethod() == null) {
            checkoutRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        }

        OrderDto order = new OrderDto();
        order.setCashierId(cashierId);
        order.setCustomerId(checkoutRequest.getCustomerId());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setDiscountId(checkoutRequest.getDiscountId());
        order.setTax(tax);
        order.setTotal(total);
        order.setPaymentMethod(checkoutRequest.getPaymentMethod());
        order.setAmountReceived(checkoutRequest.getAmountReceived());
        if (checkoutRequest.getPaymentMethod() == PaymentMethod.CASH && checkoutRequest.getAmountReceived() != null) {
            order.setChangeGiven(Math.max(0, checkoutRequest.getAmountReceived() - total));
        }
        order.setStatus(onlineOrder ? OrderStatus.PENDING : OrderStatus.COMPLETED);
        order.setOrderSource(checkoutRequest.getOrderSource());

        return CrudUtil.executeInTransaction(connection -> {
            int orderId = orderRepository.createOrder(connection, order);

            for (CartItemDto item : cartItems) {
                String displayName = item.getDisplayName();
                variantRepository.deductStock(connection, item.getVariantId(), item.getQty(), displayName);

                OrderItemDto orderItem = new OrderItemDto(
                        null, orderId, item.getVariantId(), item.getProductId(),
                        item.getProductName(), item.getSize(), item.getColor(),
                        item.getQty(), item.getUnitPrice(), item.getLineTotal()
                );
                orderRepository.addOrderItem(connection, orderItem);
                inventoryRepository.logChange(connection, item.getVariantId(), -item.getQty(), InventoryReason.SALE, cashierId);
            }

            if (checkoutRequest.getPaymentMethod() == PaymentMethod.SPLIT) {
                for (OrderPaymentDto payment : checkoutRequest.getSplitPayments()) {
                    payment.setOrderId(orderId);
                    orderRepository.addOrderPayment(connection, payment);
                }
            }

            String invoiceNo = invoiceRepository.generateNextInvoiceNo(connection);
            InvoiceDto invoice = new InvoiceDto();
            invoice.setOrderId(orderId);
            invoice.setInvoiceNo(invoiceNo);
            invoice.setSubtotal(subtotal);
            invoice.setDiscountAmount(discountAmount);
            invoice.setTax(tax);
            invoice.setTotal(total);
            invoiceRepository.createInvoice(connection, invoice);

            InvoiceDto result = invoiceRepository.findByOrderId(orderId).orElseThrow();
            result.setItems(new ArrayList<>(orderRepository.findItemsByOrderId(orderId)));
            if (checkoutRequest.getPaymentMethod() == PaymentMethod.SPLIT) {
                List<OrderPaymentDto> payments = orderRepository.findPaymentsByOrderId(orderId);
                result.setPaymentDetails(formatSplitPayments(payments));
            }
            return result;
        });
    }

    private double resolveDiscount(CheckoutRequestDto checkout, double subtotal) {
        if (checkout.getDiscountCode() != null && !checkout.getDiscountCode().isBlank()) {
            DiscountDto discount = discountService.validateCode(checkout.getDiscountCode(), subtotal);
            checkout.setDiscountId(discount.getDiscountId());
            return discountService.calculateDiscount(discount, subtotal);
        }
        if (checkout.getDiscountId() != null) {
            DiscountDto discount = discountService.findById(checkout.getDiscountId());
            return discountService.calculateDiscount(discount, subtotal);
        }
        return discountService.calculateManualDiscount(
                checkout.getManualDiscountPercent(),
                checkout.getManualDiscountFixed(),
                subtotal
        );
    }

    private void validatePayment(CheckoutRequestDto checkout, double total) {
        PaymentMethod method = checkout.getPaymentMethod() != null ? checkout.getPaymentMethod() : PaymentMethod.CASH;
        checkout.setPaymentMethod(method);

        if (method == PaymentMethod.CASH) {
            if (checkout.getAmountReceived() == null || checkout.getAmountReceived() < total) {
                throw new IllegalArgumentException("Insufficient cash received");
            }
        } else if (method == PaymentMethod.SPLIT) {
            if (checkout.getSplitPayments() == null || checkout.getSplitPayments().isEmpty()) {
                throw new IllegalArgumentException("Split payment requires at least one payment");
            }
            double sum = checkout.getSplitPayments().stream().mapToDouble(OrderPaymentDto::getAmount).sum();
            if (Math.abs(sum - total) > 0.01) {
                throw new IllegalArgumentException("Split payments must equal total");
            }
        }
    }

    private String formatSplitPayments(List<OrderPaymentDto> payments) {
        StringBuilder sb = new StringBuilder("Split: ");
        for (int i = 0; i < payments.size(); i++) {
            OrderPaymentDto p = payments.get(i);
            if (i > 0) sb.append(", ");
            sb.append(p.getMethod().name()).append(" Rs.").append(String.format("%.2f", p.getAmount()));
        }
        return sb.toString();
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<OrderDto> searchOrders(LocalDate from, LocalDate to, Integer orderId, Integer cashierId) {
        return orderRepository.searchOrders(from, to, orderId, cashierId);
    }

    @Override
    public OrderDto getOrderDetails(Integer orderId) {
        OrderDto order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setItems(orderRepository.findItemsByOrderId(orderId));
        order.setPayments(orderRepository.findPaymentsByOrderId(orderId));
        return order;
    }
}
