package edu.icet.service.Impl;

import edu.icet.config.AppConfig;
import edu.icet.model.dto.DailySalesDto;
import edu.icet.model.dto.InvoiceDto;
import edu.icet.model.dto.InvoiceLineItemDto;
import edu.icet.model.dto.OrderItemDto;
import edu.icet.model.dto.SalesSummaryDto;
import edu.icet.model.dto.TopProductDto;
import edu.icet.service.JasperReportService;
import edu.icet.util.AlertUtil;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JasperReportServiceImpl implements JasperReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] generateInvoicePdf(InvoiceDto invoice) {
        try {
            JasperPrint print = buildInvoicePrint(invoice);
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    @Override
    public byte[] generateSalesReportPdf(LocalDate from, LocalDate to, SalesSummaryDto summary,
                                         List<DailySalesDto> daily, List<TopProductDto> top) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("storeName", AppConfig.getStoreName());
            params.put("dateFrom", from.toString());
            params.put("dateTo", to.toString());
            params.put("orderCount", String.valueOf(summary.getOrderCount()));
            params.put("totalRevenue", String.format("Rs. %.2f", summary.getTotalRevenue()));
            params.put("totalTax", String.format("Rs. %.2f", summary.getTotalTax()));
            params.put("dailySales", new JRBeanCollectionDataSource(daily));
            params.put("topProducts", new JRBeanCollectionDataSource(top));
            params.put("topProductsReport", compileReport("/reports/top_products.jrxml"));
            params.put("dailySalesReport", compileReport("/reports/daily_sales.jrxml"));

            JasperPrint print = fillReport("/reports/sales_summary.jrxml", params, new JREmptyDataSource());
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate sales report PDF", e);
        }
    }

    @Override
    public void printPdf(byte[] pdfBytes) {
        try {
            Path temp = Files.createTempFile("clothify-report-", ".pdf");
            Files.write(temp, pdfBytes);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().print(temp.toFile());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to print report", e);
        }
    }

    @Override
    public void printInvoice(InvoiceDto invoice) {
        try {
            JasperPrint print = buildInvoicePrint(invoice);
            JasperPrintManager.printReport(print, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to print invoice", e);
        }
    }

    @Override
    public void previewPdf(byte[] pdfBytes, Stage owner) {
        try {
            Path temp = Files.createTempFile("clothify-preview-", ".pdf");
            Files.write(temp, pdfBytes);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(temp.toFile());
            } else {
                AlertUtil.showInfo("Preview", "PDF saved to: " + temp);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to preview PDF", e);
        }
    }

    @Override
    public void savePdf(byte[] pdfBytes, String suggestedName) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save PDF Report");
            chooser.setInitialFileName(suggestedName.endsWith(".pdf") ? suggestedName : suggestedName + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = chooser.showSaveDialog(null);
            if (file != null) {
                Files.write(file.toPath(), pdfBytes);
                AlertUtil.showInfo("Saved", "Report saved to " + file.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save PDF", e);
        }
    }

    private JasperPrint buildInvoicePrint(InvoiceDto invoice) throws Exception {
        List<InvoiceLineItemDto> lines = invoice.getItems().stream()
                .map(this::toLineItem)
                .collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put("storeName", AppConfig.getStoreName());
        params.put("invoiceNo", invoice.getInvoiceNo());
        params.put("invoiceDate", invoice.getGeneratedAt() != null
                ? invoice.getGeneratedAt().format(DATE_FMT) : "");
        params.put("cashier", invoice.getCashierName() != null ? invoice.getCashierName() : "-");
        params.put("customer", invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in");
        params.put("subtotal", String.format("Rs. %.2f", invoice.getSubtotal()));
        params.put("discount", String.format("Rs. %.2f", invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : 0));
        params.put("tax", String.format("Rs. %.2f", invoice.getTax()));
        params.put("total", String.format("Rs. %.2f", invoice.getTotal()));
        String payment = invoice.getPaymentDetails() != null ? invoice.getPaymentDetails()
                : (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : "CASH");
        params.put("payment", payment);

        return fillReport("/reports/invoice.jrxml", params, new JRBeanCollectionDataSource(lines));
    }

    private JasperReport compileReport(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Report template not found: " + resourcePath);
            }
            return JasperCompileManager.compileReport(in);
        }
    }

    private JasperPrint fillReport(String resourcePath, Map<String, Object> params,
                                   net.sf.jasperreports.engine.JRDataSource dataSource) throws Exception {
        JasperReport report = compileReport(resourcePath);
        return JasperFillManager.fillReport(report, params, dataSource);
    }

    private InvoiceLineItemDto toLineItem(OrderItemDto item) {
        return new InvoiceLineItemDto(
                item.getProductName(),
                item.getQty(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
