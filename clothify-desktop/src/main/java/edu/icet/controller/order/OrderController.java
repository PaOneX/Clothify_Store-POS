package edu.icet.controller.order;

import edu.icet.config.AppConfig;
import edu.icet.config.SessionManager;
import edu.icet.factory.DesktopServiceFactory;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.*;
import edu.icet.model.enums.ClothingSize;
import edu.icet.model.enums.PaymentMethod;
import edu.icet.service.*;
import edu.icet.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    private static final double CARD_WIDTH = 190;

    private final ProductVariantService variantService = ServiceFactory.getInstance().getProductVariantService();
    private final CategoryService categoryService = ServiceFactory.getInstance().getCategoryService();
    private final CustomerService customerService = ServiceFactory.getInstance().getCustomerService();
    private final DiscountService discountService = ServiceFactory.getInstance().getDiscountService();
    private final OrderService orderService = ServiceFactory.getInstance().getOrderService();
    private final JasperReportService jasperReportService = DesktopServiceFactory.getInstance().getJasperReportService();

    private final ObservableList<CartItemDto> cartItems = FXCollections.observableArrayList();
    private List<ProductVariantDto> allVariants;
    private List<ProductGroupDto> displayedProducts = List.of();
    private final List<VBox> productCards = new ArrayList<>();
    private int selectedCardIndex = -1;
    private Integer selectedCategoryId;
    private double appliedDiscount;
    private Integer appliedDiscountId;
    private String appliedDiscountCode;
    private Double manualDiscountPercent;
    private Double manualDiscountFixed;

    @FXML private BorderPane pageRoot;
    @FXML private ImageView imgLogo;
    @FXML private TextField txtSearch;
    @FXML private TextField txtBarcode;
    @FXML private ComboBox<CustomerDto> cmbCustomer;
    @FXML private Label lblCashier;
    @FXML private Label lblDateTime;
    @FXML private VBox categorySidebar;
    @FXML private TilePane productGrid;
    @FXML private ScrollPane productScroll;
    @FXML private ListView<CartItemDto> lstCart;
    @FXML private Label lblCartTitle;
    @FXML private Label lblCartEmpty;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTax;
    @FXML private Label lblTotal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imgLogo.setImage(ImageUtil.getLogo());
        productScroll.setFitToWidth(true);
        productGrid.prefWidthProperty().bind(productScroll.widthProperty().subtract(16));
        productScroll.widthProperty().addListener((obs, o, n) -> updateGridColumns(n.doubleValue()));

        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblCashier.setText("Cashier: " + (user.getEmployeeName() != null ? user.getEmployeeName() : user.getUsername()));
        }

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                lblDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        setupCartListView();
        setupCustomers();
        setupBarcodeScanner();
        setupKeyboardShortcuts();
        loadCategories();
        loadVariants();

        cartItems.addListener((javafx.collections.ListChangeListener<CartItemDto>) c -> {
            updateTotals();
            updateCartEmptyState();
        });
        txtSearch.textProperty().addListener((o, a, b) -> renderProducts(filterAndGroupProducts()));
        txtSearch.setOnAction(e -> {
            if (!displayedProducts.isEmpty()) selectCard(0);
        });
        updateTotals();
        updateCartEmptyState();
    }

    private void setupCustomers() {
        ObservableList<CustomerDto> customers = customerService.getAll();
        cmbCustomer.setItems(customers);
        cmbCustomer.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CustomerDto c) { return c == null ? "Walk-in" : c.getName(); }
            @Override public CustomerDto fromString(String s) { return null; }
        });
        if (!customers.isEmpty()) {
            cmbCustomer.getSelectionModel().selectFirst();
        }
    }

    private void setupBarcodeScanner() {
        txtBarcode.textProperty().addListener((o, old, code) -> {
            if (code != null && code.length() >= 8) {
                ProductVariantDto variant = variantService.findByBarcode(code.trim());
                if (variant != null) {
                    addToCart(variant, 1);
                }
                txtBarcode.clear();
            }
        });
        pageRoot.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getTarget() instanceof TextField tf && tf != txtBarcode && tf.isFocused()) return;
            if (!txtSearch.isFocused() && !txtBarcode.isFocused()) {
                txtBarcode.requestFocus();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        pageRoot.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getTarget() instanceof TextField tf && tf != txtBarcode && tf != txtSearch && tf.isFocused()) {
                return;
            }
            if (e.getCode() == KeyCode.F1) { btnNewOrder(null); e.consume(); }
            else if (e.getCode() == KeyCode.F2) { txtSearch.requestFocus(); e.consume(); }
            else if (e.getCode() == KeyCode.F3) { btnCheckout(null); e.consume(); }
            else if (e.getCode() == KeyCode.DELETE) { btnRemoveFromCart(null); e.consume(); }
            else if (e.getCode() == KeyCode.ESCAPE) { btnClearCart(null); e.consume(); }
            else if (e.getCode() == KeyCode.ENTER) {
                if (txtSearch.isFocused() && displayedProducts.isEmpty()) return;
                addSelectedProductToCart();
                e.consume();
            }
            else if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.EQUALS) { adjustSelectedCartItem(1); e.consume(); }
            else if (e.getCode() == KeyCode.MINUS) { adjustSelectedCartItem(-1); e.consume(); }
            else if (e.getCode() == KeyCode.LEFT) { moveSelection(-1, 0); e.consume(); }
            else if (e.getCode() == KeyCode.RIGHT) { moveSelection(1, 0); e.consume(); }
            else if (e.getCode() == KeyCode.UP) { moveSelection(0, -1); e.consume(); }
            else if (e.getCode() == KeyCode.DOWN) { moveSelection(0, 1); e.consume(); }
        });
    }

    private void moveSelection(int dx, int dy) {
        if (displayedProducts.isEmpty()) return;
        int cols = Math.max(1, productGrid.getPrefColumns());
        if (selectedCardIndex < 0) {
            selectCard(0);
            return;
        }
        int row = selectedCardIndex / cols;
        int col = selectedCardIndex % cols;
        col += dx;
        row += dy;
        if (col < 0) col = 0;
        if (row < 0) row = 0;
        int newIndex = row * cols + col;
        if (newIndex >= displayedProducts.size()) {
            newIndex = displayedProducts.size() - 1;
        }
        selectCard(newIndex);
    }

    private void selectCard(int index) {
        if (index < 0 || index >= productCards.size()) return;
        if (selectedCardIndex >= 0 && selectedCardIndex < productCards.size()) {
            productCards.get(selectedCardIndex).getStyleClass().remove("product-card-selected");
        }
        selectedCardIndex = index;
        VBox card = productCards.get(index);
        card.getStyleClass().add("product-card-selected");
        UiEffects.scrollIntoView(productScroll, card);
    }

    private void addSelectedProductToCart() {
        if (selectedCardIndex >= 0 && selectedCardIndex < displayedProducts.size()) {
            openSizePicker(displayedProducts.get(selectedCardIndex));
        } else if (!displayedProducts.isEmpty()) {
            openSizePicker(displayedProducts.get(0));
        }
    }

    private void adjustSelectedCartItem(int delta) {
        CartItemDto selected = lstCart.getSelectionModel().getSelectedItem();
        if (selected == null && !cartItems.isEmpty()) {
            selected = cartItems.get(cartItems.size() - 1);
            lstCart.getSelectionModel().select(selected);
        }
        if (selected != null) adjustCartQty(selected, delta);
    }

    private void setupCartListView() {
        lstCart.setItems(cartItems);
        lstCart.setCellFactory(lv -> new ListCell<>() {
            private final Label lblName = new Label();
            private final Label lblMeta = new Label();
            private final Label lblLineTotal = new Label();
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Button btnRemove = new Button("×");
            private final VBox textBox = new VBox(2, lblName, lblMeta);
            private final HBox qtyBox = new HBox(4, btnMinus, btnPlus);
            private final HBox topRow = new HBox(8);
            private final HBox bottomRow = new HBox(8);
            private final VBox row = new VBox(6, topRow, bottomRow);

            {
                lblName.getStyleClass().add("cart-item-name");
                lblMeta.getStyleClass().add("cart-item-meta");
                lblLineTotal.getStyleClass().add("cart-item-total");
                row.getStyleClass().add("cart-item-row");
                topRow.setAlignment(Pos.CENTER_LEFT);
                bottomRow.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 12, 10, 12));
                HBox.setHgrow(textBox, Priority.ALWAYS);
                btnMinus.getStyleClass().addAll("btn-icon", "clear-btn");
                btnPlus.getStyleClass().addAll("btn-icon", "add-btn");
                btnRemove.getStyleClass().addAll("btn-icon", "delete-btn");
                topRow.getChildren().addAll(textBox, lblLineTotal);
                bottomRow.getChildren().addAll(qtyBox, btnRemove);
                btnMinus.setOnAction(e -> { CartItemDto item = getItem(); if (item != null) adjustCartQty(item, -1); });
                btnPlus.setOnAction(e -> { CartItemDto item = getItem(); if (item != null) adjustCartQty(item, 1); });
                btnRemove.setOnAction(e -> { CartItemDto item = getItem(); if (item != null) cartItems.remove(item); });
            }

            @Override
            protected void updateItem(CartItemDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblName.setText(item.getProductName() != null ? item.getProductName() : "");
                    lblMeta.setText(formatCartMeta(item));
                    lblLineTotal.setText(String.format("Rs. %.2f", item.getLineTotal()));
                    setGraphic(row);
                }
            }
        });
    }

    private void adjustCartQty(CartItemDto item, int delta) {
        ProductVariantDto variant = findVariant(item.getVariantId());
        int newQty = item.getQty() + delta;
        if (newQty <= 0) { cartItems.remove(item); return; }
        if (variant != null && newQty > variant.getQtyOnHand()) {
            AlertUtil.showWarning("Cart", "Not enough stock. Available: " + variant.getQtyOnHand());
            return;
        }
        item.setQty(newQty);
        lstCart.refresh();
        updateTotals();
    }

    private ProductVariantDto findVariant(Integer variantId) {
        if (allVariants == null) return null;
        return allVariants.stream().filter(v -> v.getVariantId().equals(variantId)).findFirst().orElse(null);
    }

    @FXML void btnSearch(ActionEvent event) { renderProducts(filterAndGroupProducts()); }

    @FXML void btnRemoveFromCart(ActionEvent event) {
        CartItemDto selected = lstCart.getSelectionModel().getSelectedItem();
        if (selected != null) cartItems.remove(selected);
        else if (!cartItems.isEmpty()) cartItems.remove(cartItems.size() - 1);
    }

    @FXML void btnClearCart(ActionEvent event) { clearOrder(); }

    @FXML void btnNewOrder(ActionEvent event) { clearOrder(); }

    @FXML void btnApplyDiscount(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter coupon code or manual discount");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField txtCode = new TextField();
        TextField txtPercent = new TextField();
        TextField txtFixed = new TextField();
        grid.add(new Label("Coupon Code:"), 0, 0); grid.add(txtCode, 1, 0);
        grid.add(new Label("Manual %:"), 0, 1); grid.add(txtPercent, 1, 1);
        grid.add(new Label("Manual Fixed:"), 0, 2); grid.add(txtFixed, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    appliedDiscountId = null;
                    appliedDiscountCode = null;
                    manualDiscountPercent = null;
                    manualDiscountFixed = null;
                    double subtotal = cartItems.stream().mapToDouble(CartItemDto::getLineTotal).sum();
                    if (!txtCode.getText().isBlank()) {
                        DiscountDto d = discountService.validateCode(txtCode.getText(), subtotal);
                        appliedDiscountId = d.getDiscountId();
                        appliedDiscountCode = d.getCode();
                        appliedDiscount = discountService.calculateDiscount(d, subtotal);
                    } else {
                        if (!txtPercent.getText().isBlank()) manualDiscountPercent = Double.valueOf(txtPercent.getText());
                        if (!txtFixed.getText().isBlank()) manualDiscountFixed = Double.valueOf(txtFixed.getText());
                        appliedDiscount = discountService.calculateManualDiscount(manualDiscountPercent, manualDiscountFixed, subtotal);
                    }
                    updateTotals();
                } catch (Exception e) {
                    ExceptionHandler.handle(pageRoot, "Discount", e);
                }
            }
        });
    }

    @FXML void btnCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            AlertUtil.showWarning("Checkout", "Cart is empty.");
            return;
        }
        double subtotal = cartItems.stream().mapToDouble(CartItemDto::getLineTotal).sum();
        double discount = appliedDiscount;
        double taxable = subtotal - discount;
        double tax = taxable * AppConfig.getTaxRate();
        double total = taxable + tax;

        PaymentDialogController paymentCtrl = NavigationUtil.openModalWindow(
                "/view/Payment_Dialog.fxml", "Payment",
                (PaymentDialogController c) -> c.setTotals(subtotal, discount, tax, total));
        if (paymentCtrl == null || !paymentCtrl.isConfirmed()) return;

        try {
            CheckoutRequestDto checkout = paymentCtrl.getCheckoutRequest();
            checkout.setCustomerId(getSelectedCustomerId());
            checkout.setDiscountId(appliedDiscountId);
            checkout.setDiscountCode(appliedDiscountCode);
            checkout.setManualDiscountPercent(manualDiscountPercent);
            checkout.setManualDiscountFixed(manualDiscountFixed);

            Integer cashierId = SessionManager.getInstance().getCurrentUser().getUserId();
            InvoiceDto invoice = orderService.placeOrder(cartItems, cashierId, checkout);
            clearOrder();
            loadVariants();

            try {
                jasperReportService.printInvoice(invoice);
            } catch (Exception printEx) {
                AlertUtil.showWarning("Print", "Order saved but printing failed: " + printEx.getMessage());
            }

            NavigationUtil.openModalWindow("/view/Invoice_Preview.fxml", "Invoice", controller -> {
                if (controller instanceof InvoicePreviewController ipc) ipc.setInvoice(invoice);
            });
        } catch (Exception e) {
            ExceptionHandler.handle(pageRoot, "Checkout Failed", e);
        }
    }

    private Integer getSelectedCustomerId() {
        CustomerDto c = cmbCustomer.getSelectionModel().getSelectedItem();
        return c != null ? c.getCustomerId() : null;
    }

    private void clearOrder() {
        cartItems.clear();
        appliedDiscount = 0;
        appliedDiscountId = null;
        appliedDiscountCode = null;
        manualDiscountPercent = null;
        manualDiscountFixed = null;
        updateTotals();
    }

    private void loadCategories() {
        categorySidebar.getChildren().clear();
        categorySidebar.getChildren().add(createCategoryButton("All Categories", null));
        for (CategoryDto cat : categoryService.getAllCategories()) {
            categorySidebar.getChildren().add(createCategoryButton(cat.getCategoryName(), cat.getId()));
        }
    }

    private Button createCategoryButton(String label, Integer categoryId) {
        Button btn = new Button(label);
        btn.getStyleClass().add("category-btn");
        if (categoryId == null && selectedCategoryId == null) btn.getStyleClass().add("category-btn-selected");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            selectedCategoryId = categoryId;
            categorySidebar.getChildren().forEach(n -> {
                if (n instanceof Button b) b.getStyleClass().remove("category-btn-selected");
            });
            btn.getStyleClass().add("category-btn-selected");
            renderProducts(filterAndGroupProducts());
        });
        return btn;
    }

    private void loadVariants() {
        allVariants = variantService.getAllActiveVariants();
        renderProducts(filterAndGroupProducts());
    }

    private List<ProductVariantDto> filterVariants() {
        String term = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        return allVariants.stream()
                .filter(v -> selectedCategoryId == null || selectedCategoryId.equals(v.getCategoryId()))
                .filter(v -> term.isEmpty()
                        || (v.getProductName() != null && v.getProductName().toLowerCase().contains(term))
                        || (v.getBarcode() != null && v.getBarcode().contains(term))
                        || (v.getSku() != null && v.getSku().toLowerCase().contains(term)))
                .toList();
    }

    private List<ProductGroupDto> filterAndGroupProducts() {
        Map<Integer, List<ProductVariantDto>> byProduct = new LinkedHashMap<>();
        for (ProductVariantDto variant : filterVariants()) {
            byProduct.computeIfAbsent(variant.getProductId(), id -> new ArrayList<>()).add(variant);
        }
        List<ProductGroupDto> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<ProductVariantDto>> entry : byProduct.entrySet()) {
            groups.add(ProductGroupDto.fromVariants(entry.getKey(), entry.getValue()));
        }
        return groups;
    }

    private void renderProducts(List<ProductGroupDto> products) {
        displayedProducts = products;
        productGrid.getChildren().clear();
        productCards.clear();
        selectedCardIndex = -1;
        for (ProductGroupDto group : products) {
            VBox card = createProductCard(group);
            productCards.add(card);
            productGrid.getChildren().add(card);
        }
        if (!products.isEmpty()) selectCard(0);
    }

    private VBox createProductCard(ProductGroupDto group) {
        VBox card = new VBox(6);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(CARD_WIDTH - 10);

        ImageView image = new ImageView(ImageUtil.getProductImage(group.getImagePath(), 140, 120));
        image.setFitWidth(130); image.setFitHeight(110); image.setPreserveRatio(true);
        StackPane imageFrame = new StackPane(image);
        imageFrame.getStyleClass().add("product-image-frame");
        imageFrame.setMinSize(140, 120);

        if (group.hasMultipleVariants()) {
            Label variationBadge = new Label("⧉");
            variationBadge.getStyleClass().add("product-variation-badge");
            imageFrame.getChildren().add(variationBadge);
            StackPane.setAlignment(variationBadge, Pos.TOP_RIGHT);
        }

        if (group.isFullyOutOfStock()) {
            card.getStyleClass().add("product-card-out-of-stock");
            Label overlay = new Label("Out Of Stock");
            overlay.getStyleClass().add("out-of-stock-overlay");
            imageFrame.getChildren().add(overlay);
            StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
        }

        Label name = new Label(group.getProductName());
        name.getStyleClass().add("product-card-name");
        name.setWrapText(true); name.setMaxWidth(CARD_WIDTH - 24);

        Label price = new Label(group.getPriceLabel());
        price.getStyleClass().add("product-card-price");

        int stock = group.getTotalStock();
        Label stockLabel = new Label("Stock: " + stock);
        stockLabel.getStyleClass().add(stock <= 0 ? "badge-out-of-stock"
                : stock <= AppConfig.getLowStockThreshold() ? "badge-low-stock" : "badge-in-stock");

        card.getChildren().addAll(imageFrame, name, price, stockLabel);
        if (!group.isFullyOutOfStock()) {
            card.setOnMouseClicked(e -> {
                int idx = productCards.indexOf(card);
                if (idx >= 0) selectCard(idx);
                openSizePicker(group);
            });
            card.setStyle("-fx-cursor: hand;");
            UiEffects.applyHoverScale(card);
        }
        return card;
    }

    private void openSizePicker(ProductGroupDto group) {
        if (group.isFullyOutOfStock()) {
            return;
        }
        if (group.getVariants().size() == 1) {
            ProductVariantDto variant = group.getFirstInStockVariant();
            if (variant != null) {
                addToCart(variant, 1);
            }
            return;
        }
        SizePickerDialogController picker = NavigationUtil.openModalWindow(
                "/view/Size_Picker_Dialog.fxml",
                "Select Size",
                controller -> controller.setProductGroup(group));
        if (picker != null && picker.isConfirmed() && picker.getSelectedVariant() != null) {
            addToCart(picker.getSelectedVariant(), 1);
        }
    }

    private String formatCartMeta(CartItemDto item) {
        StringBuilder meta = new StringBuilder();
        ClothingSize clothingSize = ClothingSize.fromValue(item.getSize());
        if (clothingSize != null) {
            meta.append(clothingSize.getLabel());
        } else if (item.getSize() != null && !item.getSize().isBlank()) {
            meta.append(item.getSize());
        }
        if (item.getColor() != null && !item.getColor().isBlank()) {
            if (!meta.isEmpty()) meta.append(" · ");
            meta.append(item.getColor());
        }
        if (!meta.isEmpty()) meta.append(" · ");
        meta.append(String.format("Qty %d × Rs. %.2f", item.getQty(), item.getUnitPrice()));
        return meta.toString();
    }

    private void addToCart(ProductVariantDto variant, int qty) {
        if (qty <= 0 || variant.getQtyOnHand() <= 0) return;
        for (CartItemDto item : cartItems) {
            if (item.getVariantId().equals(variant.getVariantId())) {
                int newQty = item.getQty() + qty;
                if (newQty > variant.getQtyOnHand()) {
                    AlertUtil.showWarning("Cart", "Not enough stock.");
                    return;
                }
                item.setQty(newQty);
                lstCart.refresh();
                updateTotals();
                return;
            }
        }
        if (qty > variant.getQtyOnHand()) {
            AlertUtil.showWarning("Cart", "Not enough stock.");
            return;
        }
        cartItems.add(new CartItemDto(
                variant.getVariantId(), variant.getProductId(), variant.getProductName(),
                variant.getSize(), variant.getColor(), qty, variant.getPrice()));
    }

    private void updateGridColumns(double width) {
        productGrid.setPrefColumns(Math.max(2, (int) (width / CARD_WIDTH)));
    }

    private void updateCartEmptyState() {
        boolean empty = cartItems.isEmpty();
        lblCartEmpty.setVisible(empty);
        lblCartEmpty.setManaged(empty);
        lstCart.setVisible(!empty);
        lstCart.setManaged(!empty);
        int count = cartItems.stream().mapToInt(CartItemDto::getQty).sum();
        lblCartTitle.setText(count > 0 ? "Cart (" + count + ")" : "Cart");
    }

    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(CartItemDto::getLineTotal).sum();
        if (appliedDiscountId == null && appliedDiscountCode == null
                && manualDiscountPercent == null && manualDiscountFixed == null) {
            appliedDiscount = 0;
        } else if (appliedDiscountCode != null && appliedDiscountId != null) {
            DiscountDto d = discountService.findById(appliedDiscountId);
            appliedDiscount = discountService.calculateDiscount(d, subtotal);
        } else {
            appliedDiscount = discountService.calculateManualDiscount(manualDiscountPercent, manualDiscountFixed, subtotal);
        }
        double taxable = subtotal - appliedDiscount;
        double tax = taxable * AppConfig.getTaxRate();
        double total = taxable + tax;
        lblSubtotal.setText(String.format("Rs. %.2f", subtotal));
        lblDiscount.setText(String.format("- Rs. %.2f", appliedDiscount));
        lblTax.setText(String.format("Rs. %.2f", tax));
        lblTotal.setText(String.format("Rs. %.2f", total));
    }
}
