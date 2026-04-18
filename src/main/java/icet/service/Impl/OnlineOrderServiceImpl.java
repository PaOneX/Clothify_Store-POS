package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.*;
import edu.icet.model.enums.OrderSource;
import edu.icet.model.enums.OrderSource;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.repository.ProductVariantRepository;
import edu.icet.repository.UserRepository;
import edu.icet.service.CustomerService;
import edu.icet.service.OnlineOrderService;
import edu.icet.service.OrderService;

import java.util.ArrayList;
import java.util.List;

public class OnlineOrderServiceImpl implements OnlineOrderService {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public OnlineOrderServiceImpl(OrderService orderService,
                                  CustomerService customerService,
                                  ProductVariantRepository variantRepository,
                                  UserRepository userRepository) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OnlineOrderResponseDto placeOnlineOrder(OnlineOrderRequestDto request) {
        validateRequest(request);
        List<CartItemDto> cartItems = buildCart(request);
        Integer customerId = resolveCustomerId(request);
        Integer cashierId = userRepository.findByUsername(AppConfig.getWebCashierUsername())
                .orElseThrow(() -> new IllegalStateException("Web cashier user is not configured"))
                .getUserId();

        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setCustomerId(customerId);
        checkout.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        checkout.setOrderSource(OrderSource.ONLINE);

        InvoiceDto invoice = orderService.placeOrder(cartItems, cashierId, checkout);
        OnlineOrderResponseDto response = new OnlineOrderResponseDto();
        response.setOrderId(invoice.getOrderId());
        response.setStatus("PENDING");
        response.setTotal(invoice.getTotal());
        response.setMessage("Order placed successfully. Our team will confirm your order shortly.");
        return response;
    }

    @Override
    public OrderDto getOnlineOrderStatus(Integer orderId, String phone) {
        OrderDto order = orderService.getOrderDetails(orderId);
        CustomerDto customer = customerService.findById(order.getCustomerId());
        if (customer == null || customer.getPhone() == null || !customer.getPhone().equals(phone.trim())) {
            throw new IllegalArgumentException("Order not found");
        }
        if (order.getOrderSource() != OrderSource.ONLINE) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    private void validateRequest(OnlineOrderRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
            throw new IllegalArgumentException("Customer phone is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
    }

    private List<CartItemDto> buildCart(OnlineOrderRequestDto request) {
        List<CartItemDto> cartItems = new ArrayList<>();
        for (OnlineOrderItemDto item : request.getItems()) {
            if (item.getVariantId() == null || item.getQty() == null || item.getQty() <= 0) {
                throw new IllegalArgumentException("Each line item requires variantId and qty");
            }
            ProductVariantDto variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + item.getVariantId()));
            if (!Boolean.TRUE.equals(variant.getActive())) {
                throw new IllegalArgumentException("Variant is not available: " + variant.getProductName());
            }
            if (variant.getQtyOnHand() < item.getQty()) {
                throw new IllegalArgumentException("Not enough stock for " + variant.getProductName());
            }
            cartItems.add(new CartItemDto(
                    variant.getVariantId(),
                    variant.getProductId(),
                    variant.getProductName(),
                    variant.getSize(),
                    variant.getColor(),
                    item.getQty(),
                    variant.getPrice()
            ));
        }
        return cartItems;
    }

    private Integer resolveCustomerId(OnlineOrderRequestDto request) {
        CustomerDto existing = customerService.findByPhone(request.getCustomerPhone());
        if (existing != null) {
            existing.setName(request.getCustomerName().trim());
            if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
                existing.setEmail(request.getCustomerEmail().trim());
            }
            if (request.getCustomerAddress() != null && !request.getCustomerAddress().isBlank()) {
                existing.setAddress(request.getCustomerAddress().trim());
            }
            customerService.update(existing);
            return existing.getCustomerId();
        }
        CustomerDto customer = new CustomerDto();
        customer.setName(request.getCustomerName().trim());
        customer.setPhone(request.getCustomerPhone().trim());
        customer.setEmail(request.getCustomerEmail());
        customer.setAddress(request.getCustomerAddress());
        customer.setActive(true);
        return customerService.save(customer);
    }
}
