package edu.icet.controller.category;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.CategoryDto;
import edu.icet.service.CategoryService;
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
import edu.icet.util.UiEffects;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    private final CategoryService service = ServiceFactory.getInstance().getCategoryService();
    private final ObservableList<CategoryDto> categories = FXCollections.observableArrayList();

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TableView<CategoryDto> tblCategory;
    @FXML private TableColumn<CategoryDto, Integer> colId;
    @FXML private TableColumn<CategoryDto, String> colName;
    @FXML private TableColumn<CategoryDto, String> colDescription;
    @FXML private VBox pageRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tblCategory.setItems(categories);
        TableViewUtil.configure(tblCategory);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        loadAll();

        tblCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtId.setText(String.valueOf(newVal.getId()));
                txtName.setText(newVal.getCategoryName());
                txtDescription.setText(newVal.getDescription());
            }
        });
    }

    @FXML
    void btnAdd(ActionEvent event) {
        try {
            int id = service.addCategory(buildDto(null));
            loadAll();
            FormFieldUtil.showGeneratedId(txtId, id);
            txtName.clear();
            txtDescription.clear();
            AlertUtil.showInfo("Success", "Category added (ID: " + id + ").");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnUpdate(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Update", "Select a category to update.");
                return;
            }
            service.updateCategory(buildDto(FormFieldUtil.parseId(txtId)));
            loadAll();
            AlertUtil.showInfo("Success", "Category updated.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void btnDelete(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Delete", "Select a category to delete.");
                return;
            }
            if (AlertUtil.confirm("Delete", "Delete this category?")) {
                service.deleteCategory(FormFieldUtil.parseId(txtId));
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

    private CategoryDto buildDto(Integer id) {
        CategoryDto dto = new CategoryDto();
        dto.setId(id);
        dto.setCategoryName(txtName.getText().trim());
        dto.setDescription(txtDescription.getText().trim());
        return dto;
    }

    private void loadAll() {
        categories.setAll(service.getAllCategories());
    }

    private void clearFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        txtName.clear();
        txtDescription.clear();
    }
}
