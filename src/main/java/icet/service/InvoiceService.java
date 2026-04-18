package edu.icet.service;

import edu.icet.model.dto.InvoiceDto;

public interface InvoiceService {
    InvoiceDto getInvoiceByOrderId(Integer orderId);
}
