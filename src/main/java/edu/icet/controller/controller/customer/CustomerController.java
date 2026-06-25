package edu.icet.controller.customer;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.CustomerDto;
import edu.icet.model.dto.OrderDto;
import edu.icet.service.CustomerService;
import edu.icet.util.AlertUtil;
import edu.icet.util.FormFieldUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CustomerController implements Initializable {

    private final CustomerService service = ServiceFactory.getInstance().getCustomerService();
    private final ObservableList<CustomerDto> customers = FXCollections.observableArrayList();
    private final ObservableList<OrderDto> purchaseHistory = FXCollections.observableArrayList();
    private Consumer<CustomerDto> onCustomerSelected;
    private boolean pickerMode;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAddress;
    @FXML private CheckBox chkActive;
    @FXML private TextField txtSearch;
    @FXML private TableView<CustomerDto> tblCustomer;
    @FXML private TableColumn<CustomerDto, Integer> colId;
    @FXML private TableColumn<CustomerDto, String> colName;
    @FXML private TableColumn<CustomerDto, String> colPhone;
    @FXML private TableColumn<CustomerDto, String> colEmail;
    @FXML private TableColumn<CustomerDto, String> colAddress;
    @FXML private TableColumn<CustomerDto, Boolean> colActive;
    @FXML private TableView<OrderDto> tblPurchaseHistory;
    @FXML private TableColumn<OrderDto, Integer> colOrderId;
    @FXML private TableColumn<OrderDto, String> colOrderDate;
    @FXML private TableColumn<OrderDto, Double> colSubtotal;
    @FXML private TableColumn<OrderDto, Double> colDiscount;
    @FXML private TableColumn<OrderDto, Double> colTotal;
    @FXML private TableColumn<OrderDto, String> colStatus;
    @FXML private VBox pageRoot;

    public void setOnCustomerSelected(Consumer<CustomerDto> callback) {
        this.onCustomerSelected = callback;
    }

    public void setPickerMode(boolean pickerMode) {
        this.pickerMode = pickerMode;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getOrderDate() != null ? cell.getValue().getOrderDate().toString() : ""));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getStatus() != null ? cell.getValue().getStatus().name() : ""));

        tblCustomer.setItems(customers);
        tblPurchaseHistory.setItems(purchaseHistory);
        TableViewUtil.configure(tblCustomer);
        TableViewUtil.configure(tblPurchaseHistory);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        chkActive.setSelected(true);
        loadAll();

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setSelectedValue(newVal);
                loadPurchaseHistory(newVal.getCustomerId());
            }
        });

        tblCustomer.setOnMouseClicked(event -> {
            if (pickerMode && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                CustomerDto selected = tblCustomer.getSelectionModel().getSelectedItem();
                if (selected != null && onCustomerSelected != null) {
                    onCustomerSelected.accept(selected);
                    Stage stage = (Stage) tblCustomer.getScene().getWindow();
                    stage.close();
                }
            }
        });
    }

    @FXML
    void btnAdd(ActionEvent event) {
        try {
            int id = service.save(buildDto(null));
            loadAll();
            FormFieldUtil.showGeneratedId(txtId, id);
            txtName.clear();
            txtPhone.clear();
            txtEmail.clear();
            txtAddress.clear();
            chkActive.setSelected(true);
            AlertUtil.showInfo("Success", "Customer added (ID: " + id + ").");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnUpdate(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Update", "Select a customer to update.");
                return;
            }
            service.update(buildDto(FormFieldUtil.parseId(txtId)));
            loadAll();
            AlertUtil.showInfo("Success", "Customer updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnDelete(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Delete", "Select a customer to delete.");
                return;
            }
            if (AlertUtil.confirm("Delete", "Delete this customer?")) {
                service.delete(FormFieldUtil.parseId(txtId));
                loadAll();
                clearFields();
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnClear(ActionEvent event) {
        clearFields();
        loadAll();
    }

    @FXML
    void btnSearch(ActionEvent event) {
        customers.setAll(service.search(txtSearch.getText()));
    }

    private CustomerDto buildDto(Integer id) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(id);
        dto.setName(txtName.getText().trim());
        dto.setPhone(txtPhone.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setAddress(txtAddress.getText().trim());
        dto.setActive(chkActive.isSelected());
        return dto;
    }

    private void setSelectedValue(CustomerDto customer) {
        txtId.setText(String.valueOf(customer.getCustomerId()));
        txtName.setText(customer.getName());
        txtPhone.setText(customer.getPhone());
        txtEmail.setText(customer.getEmail());
        txtAddress.setText(customer.getAddress());
        chkActive.setSelected(Boolean.TRUE.equals(customer.getActive()));
    }

    private void loadAll() {
        customers.setAll(service.getAll());
    }

    private void loadPurchaseHistory(Integer customerId) {
        purchaseHistory.setAll(service.getPurchaseHistory(customerId));
    }

    private void clearFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        chkActive.setSelected(true);
        txtSearch.clear();
        purchaseHistory.clear();
    }
}
