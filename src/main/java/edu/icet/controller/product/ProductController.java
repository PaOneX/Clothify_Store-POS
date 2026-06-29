package edu.icet.controller.product;

import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.CategoryDto;
import edu.icet.model.dto.ProductDto;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.ClothingSize;
import edu.icet.service.CategoryService;
import edu.icet.service.ImageStorageService;
import edu.icet.service.ProductService;
import edu.icet.service.ProductVariantService;
import edu.icet.util.AlertUtil;
import edu.icet.util.FormFieldUtil;
import edu.icet.util.ImageUtil;
import edu.icet.util.NavigationUtil;
import edu.icet.util.TableViewUtil;
import edu.icet.util.UiEffects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    private final ProductService productService = ServiceFactory.getInstance().getProductService();
    private final ProductVariantService variantService = ServiceFactory.getInstance().getProductVariantService();
    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();
    private final ObservableList<ProductDto> products = FXCollections.observableArrayList();
    private final ObservableList<ProductVariantDto> variants = FXCollections.observableArrayList();
    private boolean readOnly;
    private Integer selectedSupplierId;
    private File pendingImageFile;
    private String storedImagePath;

    private final ImageStorageService imageStorage = ImageStorageService.getInstance();
    private final Map<ClothingSize, CheckBox> sizeSetChecks = new LinkedHashMap<>();

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtDescription;
    @FXML private TextField txtImagePath;
    @FXML private ImageView imgPreview;
    @FXML private ComboBox<CategoryDto> cmbCategory;
    @FXML private TextField txtSupplier;
    @FXML private TextField txtSearchName;
    @FXML private ComboBox<CategoryDto> cmbFilterCategory;
    @FXML private TableView<ProductDto> tblProduct;
    @FXML private TableColumn<ProductDto, ProductDto> colImage;
    @FXML private TableColumn<ProductDto, Integer> colid;
    @FXML private TableColumn<ProductDto, String> colName;
    @FXML private TableColumn<ProductDto, Double> colPrice;
    @FXML private TableColumn<ProductDto, Integer> colQty;
    @FXML private TableColumn<ProductDto, String> colSupplier;
    @FXML private TableColumn<ProductDto, String> colCategory;
    @FXML private TableView<ProductVariantDto> tblVariants;
    @FXML private TableColumn<ProductVariantDto, String> colVarSku;
    @FXML private TableColumn<ProductVariantDto, String> colVarSize;
    @FXML private TableColumn<ProductVariantDto, String> colVarColor;
    @FXML private TableColumn<ProductVariantDto, Double> colVarPrice;
    @FXML private TableColumn<ProductVariantDto, Integer> colVarQty;
    @FXML private TableColumn<ProductVariantDto, String> colVarBarcode;
    @FXML private TextField txtVarSku;
    @FXML private ComboBox<ClothingSize> cmbVarSize;
    @FXML private CheckBox chkCustomSize;
    @FXML private TextField txtVarSizeCustom;
    @FXML private FlowPane sizeSetPane;
    @FXML private TextField txtVarColor;
    @FXML private TextField txtVarPrice;
    @FXML private TextField txtVarQty;
    @FXML private TextField txtVarBarcode;
    @FXML private TextField txtVarId;
    @FXML private Button btnAddProduct;
    @FXML private Button btnUpdateProduct;
    @FXML private Button btnDeleteProduct;
    @FXML private Button btnSupplier;
    @FXML private Button btnCategory;
    @FXML private VBox pageRoot;

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        applyReadOnlyMode();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colImage.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView thumb = new ImageView();
            {
                thumb.setFitWidth(40);
                thumb.setFitHeight(40);
                thumb.setPreserveRatio(true);
                setGraphic(thumb);
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(ProductDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    thumb.setImage(ImageUtil.getProductImage(item.getImagePath(), 40, 40));
                    setGraphic(thumb);
                }
            }
        });
        colid.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("minPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("totalQty"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        colVarSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colVarSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colVarColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colVarPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colVarQty.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colVarBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        tblProduct.setItems(products);
        tblVariants.setItems(variants);
        TableViewUtil.configure(tblProduct);
        TableViewUtil.configure(tblVariants);
        UiEffects.applyToForm(pageRoot);
        FormFieldUtil.lockAutoIdField(txtId);
        FormFieldUtil.lockAutoIdField(txtVarId);
        setupSizeControls();
        loadCategories();
        loadAllProducts();

        tblProduct.getSelectionModel().selectedItemProperty().addListener((obs, o, p) -> {
            if (p != null) {
                setSelectedProduct(p);
                loadVariants(p.getId());
            }
        });
        tblVariants.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> {
            if (v != null) setSelectedVariant(v);
        });

        cmbCategory.setConverter(categoryConverter());
        cmbFilterCategory.setConverter(categoryConverter());
        imgPreview.setImage(ImageUtil.getProductImage(null));
        txtSearchName.setOnAction(e -> btnSearch(null));
    }

    private void setupSizeControls() {
        cmbVarSize.setItems(FXCollections.observableArrayList(ClothingSize.values()));
        cmbVarSize.getSelectionModel().select(ClothingSize.M);
        chkCustomSize.selectedProperty().addListener((obs, wasCustom, custom) -> {
            cmbVarSize.setDisable(custom);
            txtVarSizeCustom.setVisible(custom);
            txtVarSizeCustom.setManaged(custom);
        });
        for (ClothingSize size : ClothingSize.standardSizes()) {
            CheckBox box = new CheckBox(size.getLabel());
            box.setUserData(size);
            box.setSelected(true);
            sizeSetChecks.put(size, box);
            sizeSetPane.getChildren().add(box);
        }
    }

    private String resolveVariantSize() {
        if (chkCustomSize.isSelected()) {
            return txtVarSizeCustom.getText().trim();
        }
        ClothingSize selected = cmbVarSize.getSelectionModel().getSelectedItem();
        return selected != null ? selected.name() : "";
    }

    private void setVariantSize(String size) {
        ClothingSize clothingSize = ClothingSize.fromValue(size);
        if (clothingSize != null) {
            chkCustomSize.setSelected(false);
            cmbVarSize.getSelectionModel().select(clothingSize);
        } else if (size != null && !size.isBlank()) {
            chkCustomSize.setSelected(true);
            txtVarSizeCustom.setText(size);
        } else {
            chkCustomSize.setSelected(false);
            cmbVarSize.getSelectionModel().clearSelection();
            txtVarSizeCustom.clear();
        }
    }

    private javafx.util.StringConverter<CategoryDto> categoryConverter() {
        return new javafx.util.StringConverter<>() {
            @Override public String toString(CategoryDto obj) { return obj == null ? "" : obj.getCategoryName(); }
            @Override public CategoryDto fromString(String s) { return null; }
        };
    }

    private void applyReadOnlyMode() {
        if (readOnly) {
            btnAddProduct.setVisible(false); btnAddProduct.setManaged(false);
            btnUpdateProduct.setVisible(false); btnUpdateProduct.setManaged(false);
            btnDeleteProduct.setVisible(false); btnDeleteProduct.setManaged(false);
            btnSupplier.setVisible(false); btnSupplier.setManaged(false);
            btnCategory.setVisible(false); btnCategory.setManaged(false);
        }
    }

    private void loadCategories() {
        ObservableList<CategoryDto> categories = categoryService.getAllCategories();
        cmbCategory.setItems(categories);
        ObservableList<CategoryDto> filterList = FXCollections.observableArrayList();
        filterList.add(new CategoryDto(null, "All Categories", ""));
        filterList.addAll(categories);
        cmbFilterCategory.setItems(filterList);
        cmbFilterCategory.getSelectionModel().selectFirst();
    }

    private void loadAllProducts() {
        products.setAll(productService.getAllProducts());
    }

    private void loadVariants(Integer productId) {
        variants.setAll(variantService.getByProductId(productId));
    }

    private void setSelectedProduct(ProductDto product) {
        txtId.setText(String.valueOf(product.getId()));
        txtName.setText(product.getProductName());
        txtDescription.setText(product.getDescription() != null ? product.getDescription() : "");
        storedImagePath = product.getImagePath();
        pendingImageFile = null;
        txtImagePath.setText(storedImagePath != null ? storedImagePath : "");
        imgPreview.setImage(ImageUtil.getProductImage(storedImagePath));
        selectedSupplierId = product.getSupplierId();
        txtSupplier.setText(product.getSupplierName() != null ? product.getSupplierName() : "");
        if (product.getCategoryId() != null) {
            cmbCategory.getItems().stream().filter(c -> product.getCategoryId().equals(c.getId()))
                    .findFirst().ifPresent(c -> cmbCategory.getSelectionModel().select(c));
        }
    }

    private void setSelectedVariant(ProductVariantDto v) {
        txtVarId.setText(String.valueOf(v.getVariantId()));
        txtVarSku.setText(v.getSku() != null ? v.getSku() : "");
        setVariantSize(v.getSize());
        txtVarColor.setText(v.getColor() != null ? v.getColor() : "");
        txtVarPrice.setText(String.valueOf(v.getPrice()));
        txtVarQty.setText(String.valueOf(v.getQtyOnHand()));
        txtVarBarcode.setText(v.getBarcode() != null ? v.getBarcode() : "");
    }

    @FXML void btnAddProduct(ActionEvent event) {
        try {
            ProductDto dto = buildProductDto(null);
            dto.setImagePath(null);
            int productId = productService.addProduct(dto);
            String imagePath = persistProductImage(productId);
            if (imagePath != null) {
                dto.setId(productId);
                dto.setImagePath(imagePath);
                productService.editProduct(dto);
            }
            loadAllProducts();
            FormFieldUtil.showGeneratedId(txtId, productId);
            storedImagePath = imagePath;
            pendingImageFile = null;
            txtImagePath.setText(imagePath != null ? imagePath : "");
            clearProductFieldsKeepId();
            AlertUtil.showInfo("Success", "Product added (ID: " + productId + "). Add variants below.");
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnUpdateProduct(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) { AlertUtil.showWarning("Update", "Select a product."); return; }
            int productId = FormFieldUtil.parseId(txtId);
            String imagePath = persistProductImage(productId);
            if (imagePath == null) {
                imagePath = storedImagePath;
            }
            ProductDto dto = buildProductDto(productId);
            dto.setImagePath(imagePath);
            productService.editProduct(dto);
            storedImagePath = imagePath;
            pendingImageFile = null;
            txtImagePath.setText(imagePath != null ? imagePath : "");
            loadAllProducts();
            AlertUtil.showInfo("Success", "Product updated.");
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnDeleteProduct(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) return;
            if (AlertUtil.confirm("Delete", "Delete product and all variants?")) {
                productService.deleteProduct(FormFieldUtil.parseId(txtId));
                loadAllProducts();
                clearProductFields();
                variants.clear();
            }
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnAddVariant(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) { AlertUtil.showWarning("Variant", "Select or add a product first."); return; }
            int productId = FormFieldUtil.parseId(txtId);
            int variantId = variantService.addVariant(buildVariantDto(null, productId));
            loadVariants(productId);
            loadAllProducts();
            FormFieldUtil.showGeneratedId(txtVarId, variantId);
            clearVariantFieldsKeepId();
            AlertUtil.showInfo("Success", "Variant added (ID: " + variantId + ").");
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnUpdateVariant(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtVarId)) return;
            Integer productId = FormFieldUtil.hasId(txtId) ? FormFieldUtil.parseId(txtId) : null;
            variantService.updateVariant(buildVariantDto(FormFieldUtil.parseId(txtVarId), productId));
            if (productId != null) loadVariants(productId);
            loadAllProducts();
            AlertUtil.showInfo("Success", "Variant updated.");
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnDeleteVariant(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtVarId)) return;
            if (AlertUtil.confirm("Delete", "Delete this variant?")) {
                variantService.deleteVariant(FormFieldUtil.parseId(txtVarId));
                if (FormFieldUtil.hasId(txtId)) loadVariants(FormFieldUtil.parseId(txtId));
                loadAllProducts();
                clearVariantFields();
            }
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnGenerateBarcode(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtVarId)) { AlertUtil.showWarning("Barcode", "Select a variant."); return; }
            int productId = FormFieldUtil.hasId(txtId) ? FormFieldUtil.parseId(txtId) : 0;
            String barcode = variantService.generateBarcode(FormFieldUtil.parseId(txtVarId));
            txtVarBarcode.setText(barcode);
            if (productId > 0) loadVariants(productId);
            AlertUtil.showInfo("Barcode", "Generated: " + barcode);
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void btnAddSizeSet(ActionEvent event) {
        try {
            if (!FormFieldUtil.hasId(txtId)) {
                AlertUtil.showWarning("Size Set", "Select or add a product first.");
                return;
            }
            List<ClothingSize> sizes = new ArrayList<>();
            for (Map.Entry<ClothingSize, CheckBox> entry : sizeSetChecks.entrySet()) {
                if (entry.getValue().isSelected()) {
                    sizes.add(entry.getKey());
                }
            }
            if (sizes.isEmpty()) {
                AlertUtil.showWarning("Size Set", "Select at least one size.");
                return;
            }
            double price = Double.parseDouble(txtVarPrice.getText().trim());
            int qty = Integer.parseInt(txtVarQty.getText().trim());
            int productId = FormFieldUtil.parseId(txtId);
            int created = variantService.addVariantsForSizes(
                    productId,
                    txtVarSku.getText().trim(),
                    txtVarColor.getText().trim(),
                    price,
                    qty,
                    sizes
            );
            loadVariants(productId);
            loadAllProducts();
            AlertUtil.showInfo("Success", created + " variant(s) added.");
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML void btnClear(ActionEvent event) { clearProductFields(); clearVariantFields(); variants.clear(); loadAllProducts(); }

    @FXML void btnSearch(ActionEvent event) {
        try {
            Integer categoryId = null;
            CategoryDto selected = cmbFilterCategory.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getId() != null) categoryId = selected.getId();
            products.setAll(productService.searchProducts(txtSearchName.getText(), categoryId, null, null));
        } catch (Exception e) { AlertUtil.showError("Search Error", e.getMessage()); }
    }

    @FXML void btnBrowseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(txtName.getScene().getWindow());
        if (file != null) {
            pendingImageFile = file;
            txtImagePath.setText(file.getName() + " (pending upload)");
            imgPreview.setImage(ImageUtil.getProductImage(file.getAbsolutePath()));
        }
    }

    @FXML void btnCategory(ActionEvent event) {
        NavigationUtil.loadContent("/view/Category_Management.fxml");
        loadCategories();
    }

    @FXML void btnSupplier(ActionEvent event) {
        NavigationUtil.openWindow("/view/Supplier_Management.fxml", "Select Supplier", controller -> {
            if (controller instanceof edu.icet.controller.supplier.SupplierController sc) {
                sc.setPickerMode(true);
                sc.setOnSupplierSelected(s -> { selectedSupplierId = s.getId(); txtSupplier.setText(s.getName()); });
            }
        });
    }

    private ProductDto buildProductDto(Integer id) {
        CategoryDto category = cmbCategory.getSelectionModel().getSelectedItem();
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setProductName(txtName.getText().trim());
        dto.setDescription(txtDescription.getText().trim());
        dto.setImagePath(storedImagePath);
        dto.setCategoryId(category != null ? category.getId() : null);
        dto.setSupplierId(selectedSupplierId);
        return dto;
    }

    private String persistProductImage(int productId) throws Exception {
        if (pendingImageFile != null) {
            if (storedImagePath != null && storedImagePath.startsWith("products/")) {
                imageStorage.deleteStoredImage(storedImagePath);
            }
            return imageStorage.saveProductImage(pendingImageFile, productId);
        }
        return null;
    }

    private ProductVariantDto buildVariantDto(Integer variantId, Integer productId) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setVariantId(variantId);
        dto.setProductId(productId);
        dto.setSku(txtVarSku.getText().trim());
        dto.setSize(resolveVariantSize());
        dto.setColor(txtVarColor.getText().trim());
        dto.setBarcode(txtVarBarcode.getText().isBlank() ? null : txtVarBarcode.getText().trim());
        dto.setPrice(Double.valueOf(txtVarPrice.getText()));
        dto.setQtyOnHand(Integer.valueOf(txtVarQty.getText()));
        dto.setActive(true);
        return dto;
    }

    private void clearProductFields() {
        FormFieldUtil.clearAutoIdField(txtId);
        clearProductFieldsKeepId();
    }

    private void clearProductFieldsKeepId() {
        txtName.clear(); txtDescription.clear();
        txtImagePath.clear();
        storedImagePath = null;
        pendingImageFile = null;
        imgPreview.setImage(ImageUtil.getProductImage(null));
        txtSupplier.clear(); cmbCategory.getSelectionModel().clearSelection();
        selectedSupplierId = null;
    }

    private void clearVariantFields() {
        FormFieldUtil.clearAutoIdField(txtVarId);
        clearVariantFieldsKeepId();
    }

    private void clearVariantFieldsKeepId() {
        txtVarSku.clear();
        chkCustomSize.setSelected(false);
        cmbVarSize.getSelectionModel().select(ClothingSize.M);
        txtVarSizeCustom.clear();
        txtVarColor.clear();
        txtVarPrice.clear(); txtVarQty.clear(); txtVarBarcode.clear();
    }
}
