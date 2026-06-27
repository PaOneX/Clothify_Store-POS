package edu.icet;

import edu.icet.db.DatabaseMigrator;
import edu.icet.factory.ServiceFactory;
import edu.icet.util.NavigationUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        NavigationUtil.setPrimaryStage(stage);
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Login.fxml"))));
        stage.setTitle("Clothify Store - Login");
        stage.show();
        ServiceFactory.getInstance().getBackupService().scheduleAutoBackup();
        ServiceFactory.getInstance().getNotificationService().checkAndCreateAlerts();
    }

    public static void main(String[] args) {
        DatabaseMigrator.migrate();
        launch(args);
    }
}
