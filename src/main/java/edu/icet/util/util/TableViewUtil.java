package edu.icet.util;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class TableViewUtil {

    private TableViewUtil() {
    }

    /**
     * Fills the table width with all defined columns (no empty filler column).
     * Fixed columns keep their FXML pref widths; the last column expands to fill remaining space.
     */
    public static void configure(TableView<?> table) {
        if (table.getColumns().isEmpty()) {
            return;
        }

        for (TableColumn<?, ?> column : table.getColumns()) {
            if (column.getPrefWidth() <= 0) {
                column.setPrefWidth(100);
            }
            column.setMinWidth(Math.min(column.getPrefWidth(), 50));
            column.setResizable(true);
        }

        TableColumn<?, ?> lastColumn = table.getColumns().get(table.getColumns().size() - 1);
        lastColumn.setMaxWidth(Double.MAX_VALUE);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        Runnable fitColumns = () -> {
            double tableWidth = table.getWidth();
            if (tableWidth <= 0) {
                return;
            }

            double fixedTotal = 0;
            for (int i = 0; i < table.getColumns().size() - 1; i++) {
                fixedTotal += table.getColumns().get(i).getPrefWidth();
            }

            double lastWidth = tableWidth - fixedTotal - 3;
            if (lastWidth >= lastColumn.getMinWidth()) {
                lastColumn.setPrefWidth(lastWidth);
            }
        };

        table.widthProperty().addListener((obs, oldVal, newVal) -> fitColumns.run());
        table.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                Platform.runLater(fitColumns);
            }
        });
    }
}
