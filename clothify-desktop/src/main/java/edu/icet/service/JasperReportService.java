package edu.icet.service;

import edu.icet.model.dto.DailySalesDto;
import edu.icet.model.dto.InvoiceDto;
import edu.icet.model.dto.SalesSummaryDto;
import edu.icet.model.dto.TopProductDto;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public interface JasperReportService {
    byte[] generateInvoicePdf(InvoiceDto invoice);
    byte[] generateSalesReportPdf(LocalDate from, LocalDate to, SalesSummaryDto summary,
                                  List<DailySalesDto> daily, List<TopProductDto> top);
    void printPdf(byte[] pdfBytes);
    void previewPdf(byte[] pdfBytes, Stage owner);
    void savePdf(byte[] pdfBytes, String suggestedName);
    void printInvoice(InvoiceDto invoice);
}
