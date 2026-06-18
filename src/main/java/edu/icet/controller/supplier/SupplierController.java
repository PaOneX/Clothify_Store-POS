package edu.icet.controller.supplier;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.SupplierDto;
import edu.icet.service.SupplierService;
import edu.icet.util.AlertUtil;
import edu.icet.util.FormFieldUtil;
import edu.icet.util.TableViewUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import edu.icet.util.UiEffects;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SupplierController implements Initializable {

    private final SupplierService service = ServiceFactory.getInstance().getSupplierService();
    private final ObservableList<SupplierDto> suppliers = FXCollections.observableArrayList();
    private Consumer<SupplierDto> onSupplierSelected;
    private boolean pickerMode;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtMobile;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtSearch;
    @FXML private TableView<SupplierDto> tblSupplier;
    @FXML private TableColumn<SupplierDto, Integer> colId;
    @FXML private TableColumn<SupplierDto, String> colName;
    @FXML private TableColumn<SupplierDto, String> colMobile;
    @FXML private TableColumn<SupplierDto, String> colEmail;
    @FXML private TableColumn<SupplierDto, String> colAddress;
    @FXML private VBox pageRoot;

    public void setOnSupplierSelected(Consumer<SupplierDto> callback) {
        this.onSupplierSelected = callback;
    }

    public void setPickerMode(boolean pickerMode) {
        this.pickerMode = pickerMode;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMobile.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        tblSupplier.setItems(suppliers);
        TableViewUtil.configure(tblSupplier);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        loadAllSuppliers();

        tblSupplier.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setSelectedValue(newVal);
            }
        });

        tblSupplier.setOnMouseClicked(event -> {
            if (pickerMode && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SupplierDto selected = tblSupplier.getSelectionModel().getSelectedItem();
                if (selected != null && onSupplierSelected != null) {
                    onSupplierSelected.accept(selected);
                    Stage stage = (Stage) tblSupplier.getScene().getWindow();
                    stage.close();
                }
            }
        });
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        try {
            int id = service.addSupplier(buildDto(null));
            loadAllSuppliers();
            FormFieldUtil.showGeneratedId(txtId, id);
            txtName.clear();
            txtMobile.clear();
            txtEmail.clear();
            txtAddress.clear();
            AlertUtil.showInfo("Success", "Supplier added (ID: " + id + ").");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnOnActionUpdate(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Update", "Select a supplier to update.");
                return;
            }
            service.updateSupplier(buildDto(FormFieldUtil.parseId(txtId)));
            loadAllSuppliers();
            AlertUtil.showInfo("Success", "Supplier updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnOnActionDelete(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Delete", "Select a supplier to delete.");
                return;
            }
            if (AlertUtil.confirm("Delete", "Delete this supplier?")) {
                service.deleteSupplier(FormFieldUtil.parseId(txtId));
                loadAllSuppliers();
                clearFields();
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnOnActionClear(ActionEvent event) {
        clearFields();
        loadAllSuppliers();
    }

    @FXML
    void btnSearch(ActionEvent event) {
        suppliers.setAll(service.searchSuppliers(txtSearch.getText()));
    }

    private SupplierDto buildDto(Integer id) {
        SupplierDto dto = new SupplierDto();
        dto.setId(id);
        dto.setName(txtName.getText().trim());
        dto.setContact(txtMobile.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setAddress(txtAddress.getText().trim());
        return dto;
    }

    private void setSelectedValue(SupplierDto supplier) {
        txtId.setText(String.valueOf(supplier.getId()));
        txtName.setText(supplier.getName());
        txtMobile.setText(supplier.getContact());
        txtEmail.setText(supplier.getEmail());
        txtAddress.setText(supplier.getAddress());
    }

    private void loadAllSuppliers() {
        suppliers.setAll(service.getAllSuppliers());
    }

    private void clearFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        txtName.clear();
        txtMobile.clear();
        txtEmail.clear();
        txtAddress.clear();
        txtSearch.clear();
    }
}
