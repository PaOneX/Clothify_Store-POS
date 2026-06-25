package edu.icet.controller.employee;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.EmployeeDto;
import edu.icet.service.EmployeeService;
import edu.icet.util.AlertUtil;
import edu.icet.util.FormFieldUtil;
import edu.icet.util.TableViewUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import edu.icet.util.UiEffects;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EmployeeController implements Initializable {

    private final EmployeeService service = ServiceFactory.getInstance().getEmployeeService();
    private final ObservableList<EmployeeDto> employees = FXCollections.observableArrayList();

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAddress;
    @FXML private DatePicker dpHireDate;
    @FXML private CheckBox chkActive;
    @FXML private CheckBox chkCreateLogin;
    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private TextField txtSearch;
    @FXML private TableView<EmployeeDto> tblEmployee;
    @FXML private TableColumn<EmployeeDto, Integer> colId;
    @FXML private TableColumn<EmployeeDto, String> colName;
    @FXML private TableColumn<EmployeeDto, String> colPhone;
    @FXML private TableColumn<EmployeeDto, String> colEmail;
    @FXML private TableColumn<EmployeeDto, LocalDate> colHireDate;
    @FXML private TableColumn<EmployeeDto, Boolean> colActive;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        tblEmployee.setItems(employees);
        TableViewUtil.configure(tblEmployee);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        dpHireDate.setValue(LocalDate.now());
        chkActive.setSelected(true);
        loadAll();

        tblEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(String.valueOf(newVal.getEmployeeId()));
                txtName.setText(newVal.getName());
                txtPhone.setText(newVal.getPhone());
                txtEmail.setText(newVal.getEmail());
                txtAddress.setText(newVal.getAddress());
                dpHireDate.setValue(newVal.getHireDate());
                chkActive.setSelected(newVal.isActive());
            }
        });
    }

    @FXML
    void btnAdd(ActionEvent event) {
        try {
            if (chkCreateLogin.isSelected() && (txtUsername.getText().isBlank() || txtPassword.getText().isBlank())) {
                AlertUtil.showWarning("Login", "Enter username and password for staff login.");
                return;
            }
            int id = service.addEmployee(buildDto(null), chkCreateLogin.isSelected(),
                    txtUsername.getText().trim(), txtPassword.getText());
            loadAll();
            FormFieldUtil.showGeneratedId(txtId, id);
            clearFieldsExceptId();
            AlertUtil.showInfo("Success", "Employee added (ID: " + id + ").");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnUpdate(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Update", "Select an employee to update.");
                return;
            }
            service.updateEmployee(buildDto(FormFieldUtil.parseId(txtId)));
            loadAll();
            AlertUtil.showInfo("Success", "Employee updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnDelete(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Delete", "Select an employee to delete.");
                return;
            }
            if (AlertUtil.confirm("Delete", "Delete this employee and linked login?")) {
                service.deleteEmployee(FormFieldUtil.parseId(txtId));
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
        employees.setAll(service.searchEmployees(txtSearch.getText()));
    }

    private EmployeeDto buildDto(Integer id) {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(id);
        dto.setName(txtName.getText().trim());
        dto.setPhone(txtPhone.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setAddress(txtAddress.getText().trim());
        dto.setHireDate(dpHireDate.getValue());
        dto.setActive(chkActive.isSelected());
        return dto;
    }

    private void loadAll() {
        employees.setAll(service.getAllEmployees());
    }

    private void clearFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        clearFieldsExceptId();
    }

    private void clearFieldsExceptId() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        dpHireDate.setValue(LocalDate.now());
        chkActive.setSelected(true);
        chkCreateLogin.setSelected(false);
        txtUsername.clear();
        txtPassword.clear();
        txtSearch.clear();
    }
}
