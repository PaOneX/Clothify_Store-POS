package edu.icet.controller.product;

import edu.icet.model.dto.ProductDto;
import edu.icet.service.Impl.ProductServiceImpl;
import edu.icet.service.ProductService;
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

import java.net.URL;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    ObservableList<ProductDto> products = FXCollections.observableArrayList();
    ProductService service = new ProductServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colid.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        tblProduct.setItems(products);

        loadAllProducts();

        tblProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setSelectedValuetbl(newValue);
            }
        });
    }

    private void loadAllProducts() {
        tblProduct.setItems(service.getAllProducts());
    }


    private void setSelectedValuetbl(ProductDto newValue) {
        if (newValue == null) {
            return;
        }
        txtId.setText(String.valueOf(newValue.getId()));
        txtName.setText(newValue.getProductName());
        txtdesc.setText(newValue.getDescription());
        txtPrice.setText(String.valueOf(newValue.getPrice()));
        txtQty.setText(String.valueOf(newValue.getQty()));
        txtSupplier.setText(String.valueOf(newValue.getSupplierId()));
        txtCategory.setText(String.valueOf(newValue.getCategory()));

    }


    @FXML
    private TableColumn<?, ?> colCategory;

    @FXML
    private TableColumn<?, ?> colDesc;

    @FXML
    private TableColumn<?, ?> colid;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colPrice;

    @FXML
    private TableColumn<?, ?> colQty;

    @FXML
    private TableColumn<?, ?> colSupplier;

    @FXML
    private TableView<ProductDto> tblProduct;

    @FXML
    private TextField txtCategory;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtSupplier;

    @FXML
    private TextArea txtdesc;

    @FXML
    void btnAddProduct(ActionEvent event) throws Exception {
        Integer id = Integer.valueOf((txtId.getText()));
        String name = txtName.getText();
        String desc = txtdesc.getText();
        Integer qty = Integer.valueOf((txtQty.getText()));
        Double price = Double.valueOf(txtPrice.getText());
        Integer supplierId = Integer.valueOf(txtSupplier.getText());
        Integer category = Integer.valueOf(txtCategory.getText());

        service.addProduct(new ProductDto(id,name,desc,price,qty,supplierId,category));
        loadAllProducts();
    }

    @FXML
    void btnCategory(ActionEvent event) {

    }

    @FXML
    void btnClear(ActionEvent event) {
        clearFields();
        loadAllProducts();
    }

    private void clearFields() {
        txtId.clear();
        txtName.clear();
        txtdesc.clear();
        txtPrice.clear();
        txtQty.clear();
        txtSupplier.clear();
        txtCategory.clear();
    }

    @FXML
    void btnDeleteProduct(ActionEvent event) throws Exception{
        service.deleteProduct(Integer.valueOf(txtId.getText()));
        loadAllProducts();
    }

    @FXML
    void btnSupplier(ActionEvent event) {

    }

    @FXML
    void btnUpdateProduct(ActionEvent event) {

    }

}
