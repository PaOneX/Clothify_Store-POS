package edu.icet.controller.dashboard;

import edu.icet.config.AppConfig;
import edu.icet.config.SessionManager;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.*;
import edu.icet.service.InventoryService;
import edu.icet.service.NotificationService;
import edu.icet.service.ReportService;
import edu.icet.util.NavigationUtil;
import edu.icet.util.StockStatusUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private final ReportService reportService = ServiceFactory.getInstance().getReportService();
    private final NotificationService notificationService = ServiceFactory.getInstance().getNotificationService();
    private final InventoryService inventoryService = ServiceFactory.getInstance().getInventoryService();

    @FXML private Label lblWelcome;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblLowStock;
    @FXML private VBox notificationBox;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private TableView<ProductVariantDto> tblLowStock;
    @FXML private TableColumn<ProductVariantDto, String> colLowName;
    @FXML private TableColumn<ProductVariantDto, String> colLowSize;
    @FXML private TableColumn<ProductVariantDto, Integer> colLowQty;
    @FXML private TableColumn<ProductVariantDto, String> colLowStatus;
    @FXML private TableView<OrderDto> tblRecentOrders;
    @FXML private TableColumn<OrderDto, Integer> colOrderId;
    @FXML private TableColumn<OrderDto, String> colOrderTime;
    @FXML private TableColumn<OrderDto, Double> colOrderTotal;
    @FXML private TableView<TopProductDto> tblTopProducts;
    @FXML private TableColumn<TopProductDto, String> colTopName;
    @FXML private TableColumn<TopProductDto, Integer> colTopQty;
    @FXML private TableColumn<TopProductDto, Double> colTopRevenue;
    @FXML private VBox cardPlaceOrder;
    @FXML private VBox cardProducts;
    @FXML private VBox cardInventory;
    @FXML private VBox cardCategories;
    @FXML private VBox cardSuppliers;
    @FXML private VBox cardReports;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblWelcome.setText("Welcome back, "
                    + (user.getEmployeeName() != null ? user.getEmployeeName() : user.getUsername()));
        }

        setupTables();
        loadStats();
        loadSalesChart();
        loadLowStockAlerts();
        loadRecentOrders();
        loadTopProducts();
        loadNotifications();
        applyRoleVisibility();
        applyCardEffects();
    }

    private void setupTables() {
        colLowName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colLowSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colLowQty.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colLowStatus.setCellFactory(col -> StockStatusUtil.createStatusCell());
        colLowStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                StockStatusUtil.label(cell.getValue() != null ? cell.getValue().getQtyOnHand() : 0)));

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderTime.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getOrderDate() != null
                        ? cell.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
                        : ""));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colTopName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colTopQty.setCellValueFactory(new PropertyValueFactory<>("totalQtySold"));
        colTopRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        TableViewUtil.configure(tblLowStock);
        TableViewUtil.configure(tblRecentOrders);
        TableViewUtil.configure(tblTopProducts);
    }

    private void applyCardEffects() {
        for (VBox card : List.of(cardPlaceOrder, cardProducts, cardInventory, cardCategories, cardSuppliers, cardReports)) {
            if (card != null) UiEffects.applyHoverScale(card);
        }
    }

    private void applyRoleVisibility() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        setCardVisible(cardProducts, isAdmin);
        setCardVisible(cardInventory, isAdmin);
        setCardVisible(cardCategories, isAdmin);
        setCardVisible(cardSuppliers, isAdmin);
        setCardVisible(cardReports, isAdmin);
        if (tblLowStock != null) {
            tblLowStock.setVisible(isAdmin);
            tblLowStock.setManaged(isAdmin);
        }
        if (salesChart != null && salesChart.getParent() != null) {
            var chartCard = salesChart.getParent();
            chartCard.setVisible(isAdmin);
            chartCard.setManaged(isAdmin);
        }
    }

    private void setCardVisible(VBox card, boolean visible) {
        if (card != null) {
            card.setVisible(visible);
            card.setManaged(visible);
        }
    }

    private void loadStats() {
        LocalDate today = LocalDate.now();
        SalesSummaryDto todaySales = reportService.getSalesSummary(today, today);
        lblTodayOrders.setText(String.valueOf(todaySales.getOrderCount()));
        lblTodayRevenue.setText(String.format("Rs. %.2f", todaySales.getTotalRevenue()));
        lblLowStock.setText(String.valueOf(reportService.getInventorySummary().getLowStockCount()));
    }

    private void loadSalesChart() {
        if (salesChart == null) return;
        salesChart.getData().clear();
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(6);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DailySalesDto day : reportService.getDailySales(from, to)) {
            String label = day.getSaleDate() != null && day.getSaleDate().length() >= 10
                    ? day.getSaleDate().substring(5) : day.getSaleDate();
            series.getData().add(new XYChart.Data<>(label, day.getRevenue()));
        }
        salesChart.getData().add(series);
    }

    private void loadLowStockAlerts() {
        int threshold = AppConfig.getLowStockThreshold();
        List<ProductVariantDto> low = inventoryService.getInventoryVariants().stream()
                .filter(v -> v.getQtyOnHand() <= threshold)
                .sorted(Comparator.comparingInt(ProductVariantDto::getQtyOnHand))
                .limit(10)
                .toList();
        tblLowStock.setItems(FXCollections.observableArrayList(low));
    }

    private void loadRecentOrders() {
        LocalDate today = LocalDate.now();
        List<OrderDto> recent = reportService.searchOrders(today, today, null, null).stream()
                .sorted(Comparator.comparing(OrderDto::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();
        tblRecentOrders.setItems(FXCollections.observableArrayList(recent));
    }

    private void loadTopProducts() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);
        tblTopProducts.setItems(FXCollections.observableArrayList(reportService.getTopProducts(from, today).stream()
                .limit(8).toList()));
    }

    private void loadNotifications() {
        if (notificationBox == null) return;
        notificationBox.getChildren().clear();
        notificationService.checkAndCreateAlerts();
        List<NotificationDto> alerts = notificationService.getActiveNotifications();
        if (alerts.isEmpty()) return;
        Label header = new Label("Alerts");
        header.getStyleClass().add("section-title");
        notificationBox.getChildren().add(header);
        for (NotificationDto n : alerts) {
            Label lbl = new Label("⚠ " + n.getMessage());
            lbl.getStyleClass().add("notification-warning");
            lbl.setWrapText(true);
            notificationBox.getChildren().add(lbl);
        }
    }

    @FXML void openPlaceOrder(MouseEvent event) { NavigationUtil.loadContent("/view/Place_Order.fxml"); }
    @FXML void openProducts(MouseEvent event) { NavigationUtil.loadContent("/view/Product_Management.fxml"); }
    @FXML void openCategories(MouseEvent event) { NavigationUtil.loadContent("/view/Category_Management.fxml"); }
    @FXML void openInventory(MouseEvent event) { NavigationUtil.loadContent("/view/Inventory_Management.fxml"); }
    @FXML void openSuppliers(MouseEvent event) { NavigationUtil.loadContent("/view/Supplier_Management.fxml"); }
    @FXML void openEmployees(MouseEvent event) { NavigationUtil.loadContent("/view/Employee_Management.fxml"); }
    @FXML void openReports(MouseEvent event) { NavigationUtil.loadContent("/view/Reports.fxml"); }
}
