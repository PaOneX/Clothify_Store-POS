package edu.icet.controller.auth;

import edu.icet.config.SessionManager;
import edu.icet.factory.ServiceFactory;
import edu.icet.model.dto.UserDto;
import edu.icet.service.AuthService;
import edu.icet.util.AlertUtil;
import edu.icet.util.ImageUtil;
import edu.icet.util.NavigationUtil;
import edu.icet.util.UiEffects;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.util.Optional;

public class LoginController {

    private final AuthService authService = ServiceFactory.getInstance().getAuthService();

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ImageView imgLogo;

    @FXML
    public void initialize() {
        imgLogo.setImage(ImageUtil.getLogo());
        UiEffects.applyFocusScale(txtUsername);
        UiEffects.applyFocusScale(txtPassword);
    }

    @FXML
    void btnLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showWarning("Login", "Please enter username and password.");
            return;
        }

        Optional<UserDto> user = authService.login(username, password);
        if (user.isPresent()) {
            SessionManager.getInstance().login(user.get());
            NavigationUtil.switchScene("/view/AppShell.fxml", "Clothify Store");
        } else {
            AlertUtil.showError("Login Failed", "Invalid username or password.");
        }
    }
}
