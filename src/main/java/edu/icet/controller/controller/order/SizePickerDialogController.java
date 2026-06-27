package edu.icet.controller.order;

import edu.icet.model.dto.ProductGroupDto;
import edu.icet.model.dto.ProductVariantDto;
import edu.icet.model.enums.ClothingSize;
import edu.icet.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class SizePickerDialogController {

    @FXML private VBox dialogRoot;
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    @FXML private Label lblPrice;
    @FXML private FlowPane sizeButtonPane;
    @FXML private Label lblSizeInfo;
    @FXML private Button btnAdd;

    private ProductGroupDto productGroup;
    private ProductVariantDto selectedVariant;
    private boolean confirmed;
    private final Map<Button, ProductVariantDto> buttonVariants = new HashMap<>();

    public void setProductGroup(ProductGroupDto group) {
        this.productGroup = group;
        this.selectedVariant = null;
        this.confirmed = false;
        lblProductName.setText(group.getProductName());
        lblPrice.setText(group.getPriceLabel());
        imgProduct.setImage(ImageUtil.getProductImage(group.getImagePath(), 72, 72));
        buildSizeButtons();
        updateSizeInfo();
        btnAdd.setDisable(true);
    }

    private void buildSizeButtons() {
        sizeButtonPane.getChildren().clear();
        buttonVariants.clear();
        for (ProductVariantDto variant : productGroup.getVariants()) {
            Button btn = new Button(formatSizeLabel(variant));
            btn.getStyleClass().add("size-picker-btn");
            boolean inStock = variant.getQtyOnHand() != null && variant.getQtyOnHand() > 0;
            if (!inStock) {
                btn.getStyleClass().add("size-picker-btn-disabled");
                btn.setDisable(true);
            } else {
                btn.setOnAction(e -> selectVariant(variant, btn));
            }
            buttonVariants.put(btn, variant);
            sizeButtonPane.getChildren().add(btn);
        }
        if (productGroup.getVariants().size() == 1) {
            ProductVariantDto only = productGroup.getVariants().get(0);
            if (only.getQtyOnHand() != null && only.getQtyOnHand() > 0) {
                Button btn = (Button) sizeButtonPane.getChildren().get(0);
                selectVariant(only, btn);
            }
        }
    }

    private void selectVariant(ProductVariantDto variant, Button btn) {
        selectedVariant = variant;
        sizeButtonPane.getChildren().forEach(node -> {
            if (node instanceof Button b) {
                b.getStyleClass().remove("size-picker-btn-selected");
            }
        });
        btn.getStyleClass().add("size-picker-btn-selected");
        btnAdd.setDisable(false);
        updateSizeInfo();
    }

    private void updateSizeInfo() {
        if (selectedVariant == null) {
            lblSizeInfo.setText("Choose an available size.");
            return;
        }
        lblSizeInfo.setText(String.format("Rs. %.2f · Stock: %d%s",
                selectedVariant.getPrice(),
                selectedVariant.getQtyOnHand(),
                selectedVariant.getColor() != null && !selectedVariant.getColor().isBlank()
                        ? " · " + selectedVariant.getColor() : ""));
    }

    private String formatSizeLabel(ProductVariantDto variant) {
        ClothingSize clothingSize = ClothingSize.fromValue(variant.getSize());
        String sizeLabel = clothingSize != null ? clothingSize.getLabel() : variant.getSize();
        if (sizeLabel == null || sizeLabel.isBlank()) {
            sizeLabel = "—";
        }
        return sizeLabel;
    }

    @FXML
    void btnAdd() {
        if (selectedVariant != null) {
            confirmed = true;
            dialogRoot.getScene().getWindow().hide();
        }
    }

    @FXML
    void btnCancel() {
        confirmed = false;
        selectedVariant = null;
        dialogRoot.getScene().getWindow().hide();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ProductVariantDto getSelectedVariant() {
        return selectedVariant;
    }
}
