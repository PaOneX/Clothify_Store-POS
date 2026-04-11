package edu.icet.util;

import edu.icet.config.AppConfig;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public final class StockStatusUtil {

    private StockStatusUtil() {
    }

    public static String label(int qty) {
        if (qty <= 0) return "Out of Stock";
        if (qty <= AppConfig.getLowStockThreshold()) return "Low";
        return "Good";
    }

    public static String styleClass(int qty) {
        if (qty <= 0) return "stock-badge-out";
        if (qty <= AppConfig.getLowStockThreshold()) return "stock-badge-low";
        return "stock-badge-good";
    }

    public static <T> TableCell<T, String> createStatusCell() {
        return new TableCell<>() {
            private final Label badge = new Label();

            {
                badge.getStyleClass().add("stock-badge");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                int qty = 0;
                Object row = getTableRow().getItem();
                if (row instanceof edu.icet.model.dto.ProductVariantDto v) {
                    qty = v.getQtyOnHand() != null ? v.getQtyOnHand() : 0;
                }
                badge.setText(label(qty));
                badge.getStyleClass().removeAll("stock-badge-good", "stock-badge-low", "stock-badge-out");
                badge.getStyleClass().add(styleClass(qty));
            }
        };
    }
}
