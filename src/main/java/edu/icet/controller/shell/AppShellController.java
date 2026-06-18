package edu.icet.controller.shell;

import edu.icet.config.SessionManager;
import edu.icet.util.AlertUtil;
import edu.icet.util.ImageUtil;
import edu.icet.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AppShellController implements Initializable {

    @FXML private ImageView imgLogo;
    @FXML private Label lblUser;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnPlaceOrder;
    @FXML private Button btnProducts;
    @FXML private Button btnCategories;
    @FXML private Button btnInventory;
    @FXML private Button btnSuppliers;
    @FXML private Button btnEmployees;
    @FXML private Button btnCustomers;
    @FXML private Button btnDiscounts;
    @FXML private Button btnReturns;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;

    private final Map<Button, String> navRoutes = new HashMap<>();
    private Button activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NavigationUtil.setContentArea(contentArea);
        imgLogo.setImage(ImageUtil.getLogo());
        imgLogo.setFitWidth(140);
        imgLogo.setPreserveRatio(true);

        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblUser.setText((user.getEmployeeName() != null ? user.getEmployeeName() : user.getUsername())
                    + "\n" + SessionManager.getInstance().getRoleDisplayName());
        }

        navRoutes.put(btnDashboard, "/view/Dashboard.fxml");
        navRoutes.put(btnPlaceOrder, "/view/Place_Order.fxml");
        navRoutes.put(btnProducts, "/view/Product_Management.fxml");
        navRoutes.put(btnCategories, "/view/Category_Management.fxml");
        navRoutes.put(btnInventory, "/view/Inventory_Management.fxml");
        navRoutes.put(btnSuppliers, "/view/Supplier_Management.fxml");
        navRoutes.put(btnEmployees, "/view/Employee_Management.fxml");
        navRoutes.put(btnCustomers, "/view/Customer_Management.fxml");
        navRoutes.put(btnDiscounts, "/view/Discount_Management.fxml");
        navRoutes.put(btnReturns, "/view/Return_Management.fxml");
        navRoutes.put(btnReports, "/view/Reports.fxml");
        navRoutes.put(btnSettings, "/view/Settings.fxml");

        boolean isAdmin = SessionManager.getInstance().isAdmin();
        setAdminOnly(btnProducts, isAdmin);
        setAdminOnly(btnCategories, isAdmin);
        setAdminOnly(btnInventory, isAdmin);
        setAdminOnly(btnSuppliers, isAdmin);
        setAdminOnly(btnEmployees, isAdmin);
        setAdminOnly(btnCustomers, isAdmin);
        setAdminOnly(btnDiscounts, isAdmin);
        setAdminOnly(btnReturns, isAdmin);
        setAdminOnly(btnReports, isAdmin);
        setAdminOnly(btnSettings, isAdmin);

        NavigationUtil.loadContent("/view/Dashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML void btnDashboard(ActionEvent event) { navigate(btnDashboard); }
    @FXML void btnPlaceOrder(ActionEvent event) { navigate(btnPlaceOrder); }
    @FXML void btnProducts(ActionEvent event) { navigate(btnProducts); }
    @FXML void btnCategories(ActionEvent event) { navigate(btnCategories); }
    @FXML void btnInventory(ActionEvent event) { navigate(btnInventory); }
    @FXML void btnSuppliers(ActionEvent event) { navigate(btnSuppliers); }
    @FXML void btnEmployees(ActionEvent event) { navigate(btnEmployees); }
    @FXML void btnCustomers(ActionEvent event) { navigate(btnCustomers); }
    @FXML void btnDiscounts(ActionEvent event) { navigate(btnDiscounts); }
    @FXML void btnReturns(ActionEvent event) { navigate(btnReturns); }
    @FXML void btnReports(ActionEvent event) { navigate(btnReports); }
    @FXML void btnSettings(ActionEvent event) { navigate(btnSettings); }

    @FXML
    void btnLogout(ActionEvent event) {
        if (AlertUtil.confirm("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            NavigationUtil.switchScene("/view/Login.fxml", "Clothify Store - Login");
        }
    }

    private void navigate(Button button) {
        String route = navRoutes.get(button);
        if (route != null) {
            NavigationUtil.loadContent(route);
            setActive(button);
        }
    }

    private void setActive(Button button) {
        if (activeButton != null) activeButton.getStyleClass().remove("sidebar-btn-active");
        activeButton = button;
        if (!button.getStyleClass().contains("sidebar-btn-active")) {
            button.getStyleClass().add("sidebar-btn-active");
        }
    }

    private void setAdminOnly(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }
}
