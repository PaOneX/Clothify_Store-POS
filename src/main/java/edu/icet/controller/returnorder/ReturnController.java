package edu.icet.controller.returnorder;

import edu.icet.config.SessionManager;
import edu.icet.factory.RepositoryFactory;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.OrderDto;
import edu.icet.model.dto.OrderItemDto;
import edu.icet.model.dto.ReturnDto;
import edu.icet.model.dto.ReturnItemDto;
import edu.icet.model.enums.ReturnType;
import edu.icet.service.OrderService;
import edu.icet.service.ReturnService;
import edu.icet.util.AlertUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ReturnController implements Initializable {

    private final OrderService orderService = ServiceFactory.getInstance().getOrderService();
    private final ReturnService returnService = ServiceFactory.getInstance().getReturnService();

    private final ObservableList<ReturnLineItem> returnLines = FXCollections.observableArrayList();
    private OrderDto currentOrder;

    @FXML private TextField txtSearch;
    @FXML private Label lblOrderInfo;
    @FXML private TableView<ReturnLineItem> tblItems;
    @FXML private TableColumn<ReturnLineItem, Boolean> colReturn;
    @FXML private TableColumn<ReturnLineItem, String> colProduct;
    @FXML private TableColumn<ReturnLineItem, String> colVariant;
    @FXML private TableColumn<ReturnLineItem, Integer> colOrderedQty;
    @FXML private TableColumn<ReturnLineItem, Integer> colReturnQty;
    @FXML private TableColumn<ReturnLineItem, Double> colUnitPrice;
    @FXML private TextField txtReason;
    @FXML private ComboBox<ReturnType> cmbReturnType;
    @FXML private Label lblRefundEstimate;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbReturnType.setItems(FXCollections.observableArrayList(ReturnType.REFUND, ReturnType.EXCHANGE));
        cmbReturnType.getSelectionModel().selectFirst();

        colReturn.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        colReturn.setCellFactory(CheckBoxTableCell.forTableColumn(colReturn));

        colProduct.setCellValueFactory(cell -> cell.getValue().productNameProperty());
        colVariant.setCellValueFactory(cell -> cell.getValue().variantLabelProperty());
        colOrderedQty.setCellValueFactory(cell -> cell.getValue().orderedQtyProperty().asObject());
        colReturnQty.setCellValueFactory(cell -> cell.getValue().returnQtyProperty().asObject());
        colUnitPrice.setCellValueFactory(cell -> cell.getValue().unitPriceProperty().asObject());

        colReturnQty.setCellFactory(column -> new TextFieldTableCell<>(new IntegerStringConverter()) {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setDisable(true);
                    return;
                }
                setDisable(!getTableRow().getItem().isSelected());
            }
        });
        colReturnQty.setOnEditCommit(event -> {
            ReturnLineItem line = event.getRowValue();
            int max = line.getOrderedQty();
            int qty = event.getNewValue() != null ? event.getNewValue() : 0;
            line.setReturnQty(Math.min(Math.max(0, qty), max));
            if (line.getReturnQty() > 0) {
                line.setSelected(true);
            }
            updateRefundEstimate();
            tblItems.refresh();
        });

        tblItems.setItems(returnLines);
        tblItems.setEditable(true);
        TableViewUtil.configure(tblItems);
        UiEffects.applyToForm(pageRoot);

        returnLines.addListener((javafx.collections.ListChangeListener<ReturnLineItem>) change -> updateRefundEstimate());
        lblOrderInfo.setText("Search for an order by invoice number or order ID.");
        updateRefundEstimate();
    }

    @FXML
    void btnSearch(ActionEvent event) {
        try {
            String term = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
            if (term.isEmpty()) {
                AlertUtil.showWarning("Search", "Enter an invoice number or order ID.");
                return;
            }
            OrderDto order = resolveOrder(term);
            loadOrder(order);
        } catch (Exception e) {
            AlertUtil.showError("Search Failed", rootCause(e).getMessage());
        }
    }

    @FXML
    void btnProcessReturn(ActionEvent event) {
        if (currentOrder == null) {
            AlertUtil.showWarning("Return", "Load an order first.");
            return;
        }
        if (cmbReturnType.getValue() == null) {
            AlertUtil.showWarning("Return", "Select a return type.");
            return;
        }
        if (txtReason.getText() == null || txtReason.getText().isBlank()) {
            AlertUtil.showWarning("Return", "Enter a reason for the return.");
            txtReason.requestFocus();
            return;
        }

        List<ReturnItemDto> items = new ArrayList<>();
        for (ReturnLineItem line : returnLines) {
            if (!line.isSelected() || line.getReturnQty() <= 0) {
                continue;
            }
            OrderItemDto orderItem = line.getOrderItem();
            ReturnItemDto item = new ReturnItemDto();
            item.setVariantId(orderItem.getVariantId());
            item.setProductName(orderItem.getProductName());
            item.setSize(orderItem.getSize());
            item.setColor(orderItem.getColor());
            item.setQty(line.getReturnQty());
            item.setUnitPrice(orderItem.getUnitPrice());
            items.add(item);
        }

        if (items.isEmpty()) {
            AlertUtil.showWarning("Return", "Select at least one item with a return quantity.");
            return;
        }

        if (!AlertUtil.confirm("Process Return",
                String.format("Process %s for %d item(s)? Estimated refund: %s",
                        cmbReturnType.getValue(), items.size(), formatMoney(estimateRefund(items))))) {
            return;
        }

        try {
            Integer cashierId = SessionManager.getInstance().getCurrentUser().getUserId();
            ReturnDto returnDto = new ReturnDto();
            returnDto.setOrderId(currentOrder.getOrderId());
            returnDto.setReason(txtReason.getText().trim());
            returnDto.setReturnType(cmbReturnType.getValue());

            returnService.processReturn(returnDto, items, cashierId);
            AlertUtil.showInfo("Success", "Return processed successfully.");
            clearForm();
        } catch (Exception e) {
            AlertUtil.showError("Return Failed", rootCause(e).getMessage());
        }
    }

    @FXML
    void btnClear(ActionEvent event) {
        clearForm();
    }

    private OrderDto resolveOrder(String term) {
        if (term.matches("\\d+")) {
            return orderService.getOrderDetails(Integer.parseInt(term));
        }
        return RepositoryFactory.getInstance().getOrderRepository()
                .findByInvoiceNo(term)
                .map(order -> orderService.getOrderDetails(order.getOrderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found for invoice: " + term));
    }

    private void loadOrder(OrderDto order) {
        currentOrder = order;
        returnLines.clear();

        String date = order.getOrderDate() != null
                ? order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "-";
        lblOrderInfo.setText(String.format(
                "Order #%d | Date: %s | Cashier: %s | Total: %s",
                order.getOrderId(),
                date,
                order.getCashierName() != null ? order.getCashierName() : "-",
                formatMoney(order.getTotal() != null ? order.getTotal() : 0)));

        for (OrderItemDto item : order.getItems()) {
            ReturnLineItem line = new ReturnLineItem(item);
            line.selectedProperty().addListener((obs, oldVal, newVal) -> updateRefundEstimate());
            line.returnQtyProperty().addListener((obs, oldVal, newVal) -> updateRefundEstimate());
            returnLines.add(line);
        }
        updateRefundEstimate();
    }

    private void clearForm() {
        currentOrder = null;
        returnLines.clear();
        txtReason.clear();
        cmbReturnType.getSelectionModel().selectFirst();
        lblOrderInfo.setText("Search for an order by invoice number or order ID.");
        updateRefundEstimate();
    }

    private void updateRefundEstimate() {
        double refund = returnLines.stream()
                .filter(ReturnLineItem::isSelected)
                .filter(line -> line.getReturnQty() > 0)
                .mapToDouble(line -> line.getReturnQty() * line.getUnitPrice())
                .sum();
        lblRefundEstimate.setText("Estimated refund: " + formatMoney(refund));
    }

    private double estimateRefund(List<ReturnItemDto> items) {
        return items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQty()).sum();
    }

    private String formatMoney(double value) {
        return String.format("Rs. %.2f", value);
    }

    private Throwable rootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    public static final class ReturnLineItem {
        private final OrderItemDto orderItem;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final IntegerProperty returnQty = new SimpleIntegerProperty(0);
        private final IntegerProperty orderedQty = new SimpleIntegerProperty(0);
        private final javafx.beans.property.StringProperty productName =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.StringProperty variantLabel =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.DoubleProperty unitPrice =
                new javafx.beans.property.SimpleDoubleProperty();

        public ReturnLineItem(OrderItemDto orderItem) {
            this.orderItem = orderItem;
            orderedQty.set(orderItem.getQty() != null ? orderItem.getQty() : 0);
            productName.set(orderItem.getProductName() != null ? orderItem.getProductName() : "");
            String size = orderItem.getSize() != null ? orderItem.getSize() : "";
            String color = orderItem.getColor() != null ? orderItem.getColor() : "";
            variantLabel.set(size.isBlank() && color.isBlank() ? "-" : size + " / " + color);
            unitPrice.set(orderItem.getUnitPrice() != null ? orderItem.getUnitPrice() : 0);

            selected.addListener((obs, oldVal, newVal) -> {
                if (newVal && returnQty.get() == 0) {
                    returnQty.set(1);
                } else if (!newVal) {
                    returnQty.set(0);
                }
            });
            returnQty.addListener((obs, oldVal, newVal) -> {
                int max = orderedQty.get();
                if (newVal.intValue() > max) {
                    returnQty.set(max);
                }
            });
        }

        public OrderItemDto getOrderItem() {
            return orderItem;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public int getReturnQty() {
            return returnQty.get();
        }

        public void setReturnQty(int returnQty) {
            this.returnQty.set(returnQty);
        }

        public IntegerProperty returnQtyProperty() {
            return returnQty;
        }

        public int getOrderedQty() {
            return orderedQty.get();
        }

        public IntegerProperty orderedQtyProperty() {
            return orderedQty;
        }

        public javafx.beans.property.StringProperty productNameProperty() {
            return productName;
        }

        public javafx.beans.property.StringProperty variantLabelProperty() {
            return variantLabel;
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public javafx.beans.property.DoubleProperty unitPriceProperty() {
            return unitPrice;
        }
    }
}
