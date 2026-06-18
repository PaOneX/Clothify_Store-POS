package edu.icet.controller.settings;

import edu.icet.config.AppConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private Label lblStoreName;
    @FXML private Label lblTaxRate;
    @FXML private Label lblLowStock;
    @FXML private Label lblSessionTimeout;
    @FXML private Label lblBackupDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStoreName.setText(AppConfig.getStoreName());
        lblTaxRate.setText(String.format("%.1f%%", AppConfig.getTaxRate() * 100));
        lblLowStock.setText(String.valueOf(AppConfig.getLowStockThreshold()));
        lblSessionTimeout.setText(String.valueOf(AppConfig.getSessionTimeoutMinutes()));
        lblBackupDir.setText(AppConfig.getBackupDir());
    }
}
