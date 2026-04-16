package edu.icet.repository;

import edu.icet.model.dto.InvoiceDto;

import java.sql.Connection;
import java.util.Optional;

public interface InvoiceRepository {
    int createInvoice(InvoiceDto invoice);
    int createInvoice(Connection connection, InvoiceDto invoice);
    Optional<InvoiceDto> findByOrderId(Integer orderId);
    String generateNextInvoiceNo();
    String generateNextInvoiceNo(Connection connection);
}
