package edu.icet.controller.discount;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.DiscountDto;
import edu.icet.model.enums.DiscountType;
import edu.icet.service.DiscountService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DiscountController implements Initializable {

    private final DiscountService service = ServiceFactory.getInstance().getDiscountService();
    private final ObservableList<DiscountDto> discounts = FXCollections.observableArrayList();

    @FXML private TextField txtId;
    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private ComboBox<DiscountType> cmbType;
    @FXML private TextField txtValue;
    @FXML private TextField txtMinOrder;
    @FXML private DatePicker dpValidFrom;
    @FXML private DatePicker dpValidTo;
    @FXML private CheckBox chkActive;
    @FXML private TableView<DiscountDto> tblDiscount;
    @FXML private TableColumn<DiscountDto, Integer> colId;
    @FXML private TableColumn<DiscountDto, String> colCode;
    @FXML private TableColumn<DiscountDto, String> colName;
    @FXML private TableColumn<DiscountDto, DiscountType> colType;
    @FXML private TableColumn<DiscountDto, Double> colValue;
    @FXML private TableColumn<DiscountDto, Double> colMinOrder;
    @FXML private TableColumn<DiscountDto, LocalDate> colValidFrom;
    @FXML private TableColumn<DiscountDto, LocalDate> colValidTo;
    @FXML private TableColumn<DiscountDto, Boolean> colActive;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbType.setItems(FXCollections.observableArrayList(DiscountType.values()));
        cmbType.setValue(DiscountType.PERCENTAGE);

        colId.setCellValueFactory(new PropertyValueFactory<>("discountId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colMinOrder.setCellValueFactory(new PropertyValueFactory<>("minOrder"));
        colValidFrom.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        colValidTo.setCellValueFactory(new PropertyValueFactory<>("validTo"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        tblDiscount.setItems(discounts);
        TableViewUtil.configure(tblDiscount);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        chkActive.setSelected(true);
        loadAll();

        tblDiscount.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setSelectedValue(newVal);
            }
        });
    }

    @FXML
    void btnAdd(ActionEvent event) {
        try {
            int id = service.save(buildDto(null));
            loadAll();
            FormFieldUtil.showGeneratedId(txtId, id);
            txtCode.clear();
            txtName.clear();
            cmbType.setValue(DiscountType.PERCENTAGE);
            txtValue.clear();
            txtMinOrder.clear();
            dpValidFrom.setValue(null);
            dpValidTo.setValue(null);
            chkActive.setSelected(true);
            AlertUtil.showInfo("Success", "Discount added (ID: " + id + ").");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnUpdate(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Update", "Select a discount to update.");
                return;
            }
            service.update(buildDto(FormFieldUtil.parseId(txtId)));
            loadAll();
            AlertUtil.showInfo("Success", "Discount updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnDelete(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Delete", "Select a discount to delete.");
                return;
            }
            if (AlertUtil.confirm("Delete", "Delete this discount?")) {
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

    private DiscountDto buildDto(Integer id) {
        DiscountDto dto = new DiscountDto();
        dto.setDiscountId(id);
        dto.setCode(txtCode.getText().trim());
        dto.setName(txtName.getText().trim());
        dto.setType(cmbType.getValue());
        dto.setValue(parseDouble(txtValue.getText(), "Value"));
        dto.setMinOrder(txtMinOrder.getText().isBlank() ? 0.0 : parseDouble(txtMinOrder.getText(), "Min order"));
        dto.setValidFrom(dpValidFrom.getValue());
        dto.setValidTo(dpValidTo.getValue());
        dto.setActive(chkActive.isSelected());
        return dto;
    }

    private double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid number.");
        }
    }

    private void setSelectedValue(DiscountDto discount) {
        txtId.setText(String.valueOf(discount.getDiscountId()));
        txtCode.setText(discount.getCode());
        txtName.setText(discount.getName());
        cmbType.setValue(discount.getType());
        txtValue.setText(discount.getValue() != null ? String.valueOf(discount.getValue()) : "");
        txtMinOrder.setText(discount.getMinOrder() != null ? String.valueOf(discount.getMinOrder()) : "");
        dpValidFrom.setValue(discount.getValidFrom());
        dpValidTo.setValue(discount.getValidTo());
        chkActive.setSelected(Boolean.TRUE.equals(discount.getActive()));
    }

    private void loadAll() {
        discounts.setAll(service.getAll());
    }

    private void clearFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        txtCode.clear();
        txtName.clear();
        cmbType.setValue(DiscountType.PERCENTAGE);
        txtValue.clear();
        txtMinOrder.clear();
        dpValidFrom.setValue(null);
        dpValidTo.setValue(null);
        chkActive.setSelected(true);
    }
}
