package edu.icet.util;

import edu.icet.config.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public final class NavigationUtil {

    private static Stage primaryStage;
    private static StackPane contentArea;

    private NavigationUtil() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setContentArea(StackPane pane) {
        contentArea = pane;
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxmlPath, e);
        }
    }

    public static void loadContent(String fxmlPath) {
        if (contentArea == null) {
            throw new IllegalStateException("Content area not initialized");
        }
        if (!SessionManager.getInstance().canAccess(fxmlPath)) {
            AlertUtil.showWarning("Access Denied", "You do not have permission to open this section.");
            return;
        }
        try {
            Parent content = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load content " + fxmlPath, e);
        }
    }

    public static <T> T loadContent(String fxmlPath, Consumer<T> controllerSetup) {
        if (contentArea == null) {
            throw new IllegalStateException("Content area not initialized");
        }
        if (!SessionManager.getInstance().canAccess(fxmlPath)) {
            AlertUtil.showWarning("Access Denied", "You do not have permission to open this section.");
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent content = loader.load();
            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }
            contentArea.getChildren().setAll(content);
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load content " + fxmlPath, e);
        }
    }

    public static <T> T openWindow(String fxmlPath, String title, Consumer<T> controllerSetup) {
        if (!SessionManager.getInstance().canAccess(fxmlPath)) {
            AlertUtil.showWarning("Access Denied", "You do not have permission to open this section.");
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }
            stage.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to open " + fxmlPath, e);
        }
    }

    public static <T> T openModalWindow(String fxmlPath, String title, Consumer<T> controllerSetup) {
        if (!SessionManager.getInstance().canAccess(fxmlPath)) {
            AlertUtil.showWarning("Access Denied", "You do not have permission to open this section.");
            return null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(primaryStage);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }
            stage.showAndWait();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to open modal " + fxmlPath, e);
        }
    }
}
