package edu.icet.util;

import javafx.scene.control.TextField;

public final class FormFieldUtil {

    private FormFieldUtil() {
    }

    /** Locks an ID field — value comes from DB AUTO_INCREMENT only. */
    public static void lockAutoIdField(TextField field) {
        field.setEditable(false);
        field.setDisable(false);
        field.setPromptText(null);
        if (!field.getStyleClass().contains("field-locked")) {
            field.getStyleClass().add("field-locked");
        }
    }

    public static void showGeneratedId(TextField field, int id) {
        field.setText(String.valueOf(id));
    }

    public static void clearAutoIdField(TextField field) {
        field.clear();
    }

    public static boolean hasId(TextField field) {
        String text = field.getText();
        return text != null && !text.isBlank();
    }

    public static int parseId(TextField field) {
        return Integer.parseInt(field.getText().trim());
    }
}
