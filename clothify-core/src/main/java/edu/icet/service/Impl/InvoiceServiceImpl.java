package edu.icet.service.Impl;

import edu.icet.model.dto.InvoiceDto;
import edu.icet.repository.InvoiceRepository;
import edu.icet.repository.OrderRepository;
import edu.icet.service.InvoiceService;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public InvoiceDto getInvoiceByOrderId(Integer orderId) {
        InvoiceDto invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        invoice.setItems(orderRepository.findItemsByOrderId(orderId));
        return invoice;
    }
}
