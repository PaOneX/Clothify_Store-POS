package edu.icet.util;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public final class UiEffects {

    private static final double FOCUS_SCALE = 1.015;
    private static final Duration TRANSITION_DURATION = Duration.millis(120);

    private UiEffects() {
    }

    public static void applyFocusScale(Control control) {
        if (!control.getStyleClass().contains("modern-input")) {
            control.getStyleClass().add("modern-input");
        }
        control.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            ScaleTransition transition = new ScaleTransition(TRANSITION_DURATION, control);
            transition.setToX(isFocused ? FOCUS_SCALE : 1.0);
            transition.setToY(isFocused ? FOCUS_SCALE : 1.0);
            transition.play();
        });
    }

    public static void applyToForm(Parent root) {
        walkAndApply(root);
    }

    public static void applyHoverScale(Node node) {
        ScaleTransition enter = new ScaleTransition(Duration.millis(100), node);
        enter.setToX(1.03);
        enter.setToY(1.03);
        ScaleTransition exit = new ScaleTransition(Duration.millis(100), node);
        exit.setToX(1.0);
        exit.setToY(1.0);
        node.setOnMouseEntered(e -> enter.playFromStart());
        node.setOnMouseExited(e -> exit.playFromStart());
    }

    public static void scrollIntoView(ScrollPane scrollPane, Node node) {
        if (scrollPane == null || node == null) return;
        javafx.application.Platform.runLater(() -> {
            double height = scrollPane.getContent().getBoundsInLocal().getHeight();
            double viewport = scrollPane.getViewportBounds().getHeight();
            if (height <= viewport) return;
            double nodeY = node.localToScene(0, 0).getY()
                    - scrollPane.localToScene(0, 0).getY()
                    + scrollPane.getVvalue() * (height - viewport);
            double target = nodeY / (height - viewport);
            scrollPane.setVvalue(Math.max(0, Math.min(1, target)));
        });
    }

    private static void walkAndApply(Node node) {
        if (node instanceof TextField textField) {
            applyFocusScale(textField);
        } else if (node instanceof PasswordField passwordField) {
            applyFocusScale(passwordField);
        } else if (node instanceof TextArea textArea) {
            applyFocusScale(textArea);
        } else if (node instanceof ComboBox<?> comboBox) {
            applyFocusScale(comboBox);
        } else if (node instanceof DatePicker datePicker) {
            applyFocusScale(datePicker);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                walkAndApply(child);
            }
        }
    }
}
