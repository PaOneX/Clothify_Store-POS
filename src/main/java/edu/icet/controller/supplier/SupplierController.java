package edu.icet.controller.supplier;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import edu.icet.model.dto.SupplierDto;
import edu.icet.service.Impl.SupplierServiceImpl;
import edu.icet.service.SupplierService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SupplierController implements Initializable {

    ObservableList<SupplierDto> supplierDos = FXCollections.observableArrayList();
    SupplierService service = new SupplierServiceImpl();

    // Callback to pass selected supplier back to ProductController
    private Consumer<SupplierDto> onSupplierSelected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMobile.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        tblSupplier.setItems(supplierDos);

        try {
            loadAllSuppliers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        tblSupplier.getSelectionModel().selectedItemProperty().addListener((observableValue, supplierDto, newValue) -> {
            if(newValue !=null){
                setSelectedValue(newValue);
            }
        });

        // Add double-click event handler
        tblSupplier.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SupplierDto selectedSupplier = tblSupplier.getSelectionModel().getSelectedItem();
                if (selectedSupplier != null && onSupplierSelected != null) {
                    // Call the callback to pass supplier back to ProductController
                    onSupplierSelected.accept(selectedSupplier);
                    // Close the supplier window
                    Stage stage = (Stage) tblSupplier.getScene().getWindow();
                    stage.close();
                }
            }
        });
    }

    private void setSelectedValue(SupplierDto newValue) {
        if(newValue == null){
            return;
        }
        txtId.setText(String.valueOf(newValue.getId()));
        txtName.setText(newValue.getName());
        txtAddress.setText(newValue.getAddress());
        txtEmail.setText(newValue.getEmail());
        txtMobile.setText(newValue.getContact());
    }

    // Method to set the callback from ProductController
    public void setOnSupplierSelected(Consumer<SupplierDto> callback) {
        this.onSupplierSelected = callback;
    }

    private void loadAllSuppliers() throws Exception {
        supplierDos.clear();
        tblSupplier.setItems(service.getAllSuppliers());
    }

    @FXML
    private TableColumn<?, ?> colAddress;

    @FXML
    private TableColumn<?, ?> colEmail;

    @FXML
    private TableColumn<?, ?> colId;

    @FXML
    private TableColumn<?, ?> colMobile;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableView<SupplierDto> tblSupplier;

    @FXML
    private JFXTextArea txtAddress;

    @FXML
    private JFXTextField txtEmail;

    @FXML
    private JFXTextField txtId;

    @FXML
    private JFXTextField txtMobile;

    @FXML
    private JFXTextField txtName;

    @FXML
    void btnOnActionAdd(ActionEvent event) throws Exception {
        String id = txtId.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        String email = txtEmail.getText();
        String mobile = txtMobile.getText();

        service.addSupplier(new SupplierDto(id, name, address, email, mobile));
        loadAllSuppliers();

    }

    @FXML
    void btnOnActionClear(ActionEvent event) {

    }

    @FXML
    void btnOnActionDelete(ActionEvent event) {

    }

    @FXML
    void btnOnActionUpdate(ActionEvent event) {

    }

}
