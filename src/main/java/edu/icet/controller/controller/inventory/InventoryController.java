package edu.icet.controller.inventory;

import edu.icet.config.AppConfig;
import edu.icet.config.SessionManager;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.InventoryReason;
import edu.icet.service.InventoryService;
import edu.icet.util.AlertUtil;
import edu.icet.util.StockStatusUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    private final InventoryService service = ServiceFactory.getInstance().getInventoryService();
    private final ObservableList<ProductVariantDto> variants = FXCollections.observableArrayList();
    private final ObservableList<ProductVariantDto> allVariants = FXCollections.observableArrayList();

    @FXML private TableView<ProductVariantDto> tblInventory;
    @FXML private TableColumn<ProductVariantDto, Integer> colId;
    @FXML private TableColumn<ProductVariantDto, String> colName;
    @FXML private TableColumn<ProductVariantDto, String> colSize;
    @FXML private TableColumn<ProductVariantDto, Integer> colQty;
    @FXML private TableColumn<ProductVariantDto, String> colStatus;
    @FXML private TableColumn<ProductVariantDto, String> colCategory;
    @FXML private TableColumn<ProductVariantDto, String> colSupplier;
    @FXML private TextField txtSearch;
    @FXML private TextField txtAdjustQty;
    @FXML private ComboBox<String> cmbReason;
    @FXML private Label lblGoodCount;
    @FXML private Label lblLowCount;
    @FXML private Label lblOutCount;
    @FXML private Label lblSupplierDetail;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("variantId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colStatus.setCellFactory(col -> StockStatusUtil.createStatusCell());
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                StockStatusUtil.label(cell.getValue() != null ? cell.getValue().getQtyOnHand() : 0)));

        tblInventory.setItems(variants);
        TableViewUtil.configure(tblInventory);
        UiEffects.applyToForm(pageRoot);
        cmbReason.setItems(FXCollections.observableArrayList("STOCK_IN", "STOCK_OUT", "ADJUSTMENT"));
        cmbReason.getSelectionModel().selectFirst();

        int threshold = AppConfig.getLowStockThreshold();
        tblInventory.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ProductVariantDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getQtyOnHand() <= 0) {
                    setStyle("-fx-background-color: #fee2e2;");
                } else if (item.getQtyOnHand() <= threshold) {
                    setStyle("-fx-background-color: #fef9c3;");
                } else {
                    setStyle("-fx-background-color: #f0fdf4;");
                }
            }
        });

        tblInventory.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> updateSupplierDetail(v));
        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        txtSearch.setOnAction(e -> applyFilter());
        loadVariants();
    }

    @FXML void btnSearch(ActionEvent event) { applyFilter(); }

    @FXML void btnRefresh(ActionEvent event) { loadVariants(); }

    @FXML void btnStockIn(ActionEvent event) { adjustStock(true); }

    @FXML void btnStockOut(ActionEvent event) { adjustStock(false); }

    private void applyFilter() {
        String term = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        if (term.isEmpty()) {
            variants.setAll(allVariants);
        } else {
            variants.setAll(allVariants.stream()
                    .filter(v -> matches(v, term))
                    .toList());
        }
        updateStockCounts();
    }

    private boolean matches(ProductVariantDto v, String term) {
        return contains(v.getProductName(), term)
                || contains(v.getSku(), term)
                || contains(v.getSupplierName(), term)
                || contains(v.getCategoryName(), term)
                || contains(v.getSize(), term);
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private void updateSupplierDetail(ProductVariantDto v) {
        if (v == null) {
            lblSupplierDetail.setText("Select a row to view supplier details");
            return;
        }
        String supplier = v.getSupplierName() != null ? v.getSupplierName() : "No supplier assigned";
        lblSupplierDetail.setText(String.format("Supplier: %s  |  SKU: %s  |  Status: %s",
                supplier,
                v.getSku() != null ? v.getSku() : "—",
                StockStatusUtil.label(v.getQtyOnHand())));
    }

    private void adjustStock(boolean stockIn) {
        ProductVariantDto selected = tblInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Inventory", "Select a variant.");
            return;
        }
        try {
            int qty = Integer.parseInt(txtAdjustQty.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
            int change = stockIn ? qty : -qty;
            InventoryReason reason = InventoryReason.valueOf(cmbReason.getSelectionModel().getSelectedItem());
            Integer userId = SessionManager.getInstance().getCurrentUser().getUserId();
            service.adjustStock(selected.getVariantId(), change, reason, userId);
            loadVariants();
            txtAdjustQty.clear();
            AlertUtil.showInfo("Success", "Stock updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    private void loadVariants() {
        allVariants.setAll(service.getInventoryVariants());
        applyFilter();
    }

    private void updateStockCounts() {
        int threshold = AppConfig.getLowStockThreshold();
        int good = 0, low = 0, out = 0;
        for (ProductVariantDto v : allVariants) {
            int qty = v.getQtyOnHand() != null ? v.getQtyOnHand() : 0;
            if (qty <= 0) out++;
            else if (qty <= threshold) low++;
            else good++;
        }
        lblGoodCount.setText(String.valueOf(good));
        lblLowCount.setText(String.valueOf(low));
        lblOutCount.setText(String.valueOf(out));
    }
}
