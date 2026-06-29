package edu.icet.controller.order;

import edu.icet.config.AppConfig;
import edu.icet.factory.DesktopServiceFactory;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.InvoiceDto;
import edu.icet.model.dto.OrderItemDto;
import edu.icet.service.JasperReportService;
import edu.icet.util.AlertUtil;
import edu.icet.util.TableViewUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class InvoicePreviewController implements Initializable {

    private final JasperReportService jasperReportService = DesktopServiceFactory.getInstance().getJasperReportService();
    private InvoiceDto currentInvoice;

    @FXML private Label lblStoreName;
    @FXML private Label lblInvoiceNo;
    @FXML private Label lblDate;
    @FXML private Label lblCashier;
    @FXML private Label lblCustomer;
    @FXML private Label lblPayment;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTax;
    @FXML private Label lblTotal;
    @FXML private TableView<OrderItemDto> tblItems;
    @FXML private TableColumn<OrderItemDto, String> colItemName;
    @FXML private TableColumn<OrderItemDto, Integer> colItemQty;
    @FXML private TableColumn<OrderItemDto, Double> colItemPrice;
    @FXML private TableColumn<OrderItemDto, Double> colItemTotal;
    @FXML private VBox rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colItemTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        TableViewUtil.configure(tblItems);
        lblStoreName.setText(AppConfig.getStoreName());
    }

    public void setInvoice(InvoiceDto invoice) {
        this.currentInvoice = invoice;
        lblInvoiceNo.setText("Invoice: " + invoice.getInvoiceNo());
        lblDate.setText("Date: " + invoice.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lblCashier.setText("Cashier: " + (invoice.getCashierName() != null ? invoice.getCashierName() : "-"));
        if (lblCustomer != null) {
            lblCustomer.setText("Customer: " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : "Walk-in"));
        }
        if (lblPayment != null) {
            String pay = invoice.getPaymentDetails() != null ? invoice.getPaymentDetails()
                    : (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : "CASH");
            lblPayment.setText("Payment: " + pay);
        }
        lblSubtotal.setText(String.format("Subtotal: Rs. %.2f", invoice.getSubtotal()));
        if (lblDiscount != null) {
            lblDiscount.setText(String.format("Discount: Rs. %.2f",
                    invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : 0));
        }
        lblTax.setText(String.format("Tax: Rs. %.2f", invoice.getTax()));
        lblTotal.setText(String.format("Total: Rs. %.2f", invoice.getTotal()));
        tblItems.getItems().setAll(invoice.getItems());
    }

    @FXML void btnPrint(ActionEvent event) {
        if (currentInvoice != null) {
            try {
                jasperReportService.printInvoice(currentInvoice);
            } catch (Exception e) {
                AlertUtil.showError("Print", "Print failed: " + e.getMessage());
            }
        }
    }

    @FXML void btnClose(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}
