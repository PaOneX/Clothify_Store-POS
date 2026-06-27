package edu.icet.controller.report;

import edu.icet.factory.DesktopServiceFactory;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.*;
import edu.icet.service.BackupService;
import edu.icet.service.InvoiceService;
import edu.icet.service.JasperReportService;
import edu.icet.service.ReportService;
import edu.icet.util.AlertUtil;
import edu.icet.util.CsvExportUtil;
import edu.icet.util.NavigationUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import edu.icet.controller.order.InvoicePreviewController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    private final ReportService reportService = ServiceFactory.getInstance().getReportService();
    private final InvoiceService invoiceService = ServiceFactory.getInstance().getInvoiceService();
    private final JasperReportService jasperReportService = DesktopServiceFactory.getInstance().getJasperReportService();
    private final BackupService backupService = ServiceFactory.getInstance().getBackupService();

    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label lblOrderCount;
    @FXML private Label lblRevenue;
    @FXML private Label lblTax;
    @FXML private Label lblProfit;
    @FXML private Label lblSkus;
    @FXML private Label lblUnits;
    @FXML private Label lblLowStock;
    @FXML private TableView<TopProductDto> tblTopProducts;
    @FXML private TableColumn<TopProductDto, String> colTopName;
    @FXML private TableColumn<TopProductDto, Integer> colTopQty;
    @FXML private TableColumn<TopProductDto, Double> colTopRevenue;
    @FXML private TableView<DailySalesDto> tblDailySales;
    @FXML private TableColumn<DailySalesDto, String> colDailyDate;
    @FXML private TableColumn<DailySalesDto, Integer> colDailyOrders;
    @FXML private TableColumn<DailySalesDto, Double> colDailyRevenue;
    @FXML private TableView<SlowMovingDto> tblSlowMoving;
    @FXML private TableColumn<SlowMovingDto, String> colSlowName;
    @FXML private TableColumn<SlowMovingDto, Integer> colSlowDays;
    @FXML private TableColumn<SlowMovingDto, Integer> colSlowQty;
    @FXML private TextField txtOrderId;
    @FXML private TableView<OrderDto> tblOrders;
    @FXML private TableColumn<OrderDto, Integer> colOrderId;
    @FXML private TableColumn<OrderDto, String> colOrderDate;
    @FXML private TableColumn<OrderDto, String> colCashier;
    @FXML private TableColumn<OrderDto, Double> colOrderTotal;
    @FXML private VBox chartContainer;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());

        colTopName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colTopQty.setCellValueFactory(new PropertyValueFactory<>("totalQtySold"));
        colTopRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colDailyDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colDailyOrders.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        colDailyRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        colSlowName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSlowDays.setCellValueFactory(new PropertyValueFactory<>("daysSinceLastSale"));
        colSlowQty.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getOrderDate() != null ? cell.getValue().getOrderDate().toString() : ""));
        colCashier.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        TableViewUtil.configure(tblTopProducts);
        TableViewUtil.configure(tblDailySales);
        TableViewUtil.configure(tblSlowMoving);
        TableViewUtil.configure(tblOrders);
        UiEffects.applyToForm(pageRoot);

        tblOrders.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                OrderDto order = tblOrders.getSelectionModel().getSelectedItem();
                if (order != null) {
                    var invoice = invoiceService.getInvoiceByOrderId(order.getOrderId());
                    NavigationUtil.openModalWindow("/view/Invoice_Preview.fxml", "Invoice", controller -> {
                        if (controller instanceof InvoicePreviewController ipc) ipc.setInvoice(invoice);
                    });
                }
            }
        });
        loadReports();
    }

    @FXML void btnGenerate(ActionEvent event) { loadReports(); }

    @FXML void btnGenerateReport(ActionEvent event) {
        try {
            LocalDate from = dpFrom.getValue();
            LocalDate to = dpTo.getValue();
            SalesSummaryDto summary = reportService.getSalesSummary(from, to);
            List<DailySalesDto> daily = reportService.getDailySales(from, to);
            List<TopProductDto> top = reportService.getTopProducts(from, to);
            byte[] pdf = jasperReportService.generateSalesReportPdf(from, to, summary, daily, top);
            jasperReportService.previewPdf(pdf, getStage());
            jasperReportService.printPdf(pdf);
        } catch (Exception e) { AlertUtil.showError("Report", e.getMessage()); }
    }

    @FXML void btnExportPdf(ActionEvent event) {
        try {
            LocalDate from = dpFrom.getValue();
            LocalDate to = dpTo.getValue();
            SalesSummaryDto summary = reportService.getSalesSummary(from, to);
            byte[] pdf = jasperReportService.generateSalesReportPdf(from, to, summary,
                    reportService.getDailySales(from, to), reportService.getTopProducts(from, to));
            jasperReportService.savePdf(pdf, "sales-report-" + from + "-" + to);
        } catch (Exception e) { AlertUtil.showError("Export", e.getMessage()); }
    }

    @FXML void btnExportCsv(ActionEvent event) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Daily Sales CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(pageRoot.getScene().getWindow());
        if (file != null) {
            CsvExportUtil.exportDailySales(file, tblDailySales.getItems());
            AlertUtil.showInfo("Export", "CSV saved.");
        }
    }

    @FXML void btnBackup(ActionEvent event) {
        try {
            File backup = backupService.createBackup();
            AlertUtil.showInfo("Backup", "Backup created: " + backup.getAbsolutePath());
        } catch (Exception e) { AlertUtil.showError("Backup", e.getMessage()); }
    }

    @FXML void btnRestore(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL", "*.sql"));
        File file = chooser.showOpenDialog(pageRoot.getScene().getWindow());
        if (file != null && AlertUtil.confirm("Restore", "Restore database from backup? This cannot be undone.")) {
            try {
                backupService.restoreBackup(file);
                AlertUtil.showInfo("Restore", "Database restored. Please restart the application.");
            } catch (Exception e) { AlertUtil.showError("Restore", e.getMessage()); }
        }
    }

    @FXML void btnSearchOrders(ActionEvent event) {
        Integer orderId = txtOrderId.getText().isBlank() ? null : Integer.valueOf(txtOrderId.getText());
        tblOrders.setItems(FXCollections.observableArrayList(
                reportService.searchOrders(dpFrom.getValue(), dpTo.getValue(), orderId, null)));
    }

    private void loadReports() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        SalesSummaryDto sales = reportService.getSalesSummary(from, to);
        lblOrderCount.setText(String.valueOf(sales.getOrderCount()));
        lblRevenue.setText(String.format("Rs. %.2f", sales.getTotalRevenue()));
        lblTax.setText(String.format("Rs. %.2f", sales.getTotalTax()));
        ProfitSummaryDto profit = reportService.getProfitSummary(from, to);
        if (lblProfit != null) lblProfit.setText(String.format("Rs. %.2f", profit.getProfit()));
        InventorySummaryDto inventory = reportService.getInventorySummary();
        lblSkus.setText(String.valueOf(inventory.getTotalSkus()));
        lblUnits.setText(String.valueOf(inventory.getTotalUnits()));
        lblLowStock.setText(String.valueOf(inventory.getLowStockCount()));
        tblTopProducts.setItems(FXCollections.observableArrayList(reportService.getTopProducts(from, to)));
        tblDailySales.setItems(FXCollections.observableArrayList(reportService.getDailySales(from, to)));
        if (tblSlowMoving != null) {
            tblSlowMoving.setItems(FXCollections.observableArrayList(reportService.getSlowMovingProducts(30)));
        }
        tblOrders.setItems(FXCollections.observableArrayList(reportService.searchOrders(from, to, null, null)));
        buildCharts(from, to);
    }

    private void buildCharts(LocalDate from, LocalDate to) {
        if (chartContainer == null) return;
        chartContainer.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue");
        LineChart<String, Number> monthlyChart = new LineChart<>(xAxis, yAxis);
        monthlyChart.setTitle("Monthly Sales Trend");
        monthlyChart.setLegendVisible(false);
        monthlyChart.setPrefHeight(220);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (MonthlySalesDto m : reportService.getMonthlySales(from, to)) {
            series.getData().add(new XYChart.Data<>(m.getMonth(), m.getRevenue()));
        }
        monthlyChart.getData().add(series);

        CategoryAxis barX = new CategoryAxis();
        NumberAxis barY = new NumberAxis();
        BarChart<String, Number> topChart = new BarChart<>(barX, barY);
        topChart.setTitle("Top Products");
        topChart.setLegendVisible(false);
        topChart.setPrefHeight(220);
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        for (TopProductDto t : reportService.getTopProducts(from, to)) {
            String name = t.getProductName().length() > 15 ? t.getProductName().substring(0, 15) + "…" : t.getProductName();
            topSeries.getData().add(new XYChart.Data<>(name, t.getTotalQtySold()));
        }
        topChart.getData().add(topSeries);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Revenue by Category");
        pieChart.setPrefHeight(220);
        for (CategoryRevenueDto c : reportService.getRevenueByCategory(from, to)) {
            pieChart.getData().add(new PieChart.Data(c.getCategoryName(), c.getRevenue()));
        }

        HBox charts = new HBox(16, monthlyChart, topChart, pieChart);
        charts.setFillHeight(true);
        HBox.setHgrow(monthlyChart, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(topChart, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(pieChart, javafx.scene.layout.Priority.ALWAYS);
        chartContainer.getChildren().add(charts);
    }

    private Stage getStage() {
        return tblOrders.getScene() != null ? (Stage) tblOrders.getScene().getWindow() : null;
    }
}
