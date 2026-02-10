package edu.icet.controller.dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    Stage stage = new Stage();

    @FXML
    void btnCategoryManagement(ActionEvent event) {

    }

    @FXML
    void btnIManagementnventory(ActionEvent event) {

    }

    @FXML
    void btnPlaceOrder(ActionEvent event) {

    }

    @FXML
    void btnProductManagement(ActionEvent event) {
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Product_Managemnt.fxml"))));
            stage.show();


        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    @FXML
    public void btnAddSupplier(ActionEvent actionEvent) {
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Supplier_Management.fxml"))));
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


