package edu.icet.config;

import edu.icet.model.dto.UserDto;
import edu.icet.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public final class SessionManager {

    private static final Set<String> STAFF_ROUTES = Set.of(
            "/view/Dashboard.fxml",
            "/view/Place_Order.fxml",
            "/view/Invoice_Preview.fxml",
            "/view/Customer_Management.fxml",
            "/view/Payment_Dialog.fxml"
    );

    private static SessionManager instance;
    private UserDto currentUser;
    private LocalDateTime lastActivity;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(UserDto user) {
        if (user != null) {
            user.setPasswordHash(null);
        }
        this.currentUser = user;
        touch();
    }

    public void logout() {
        this.currentUser = null;
        this.lastActivity = null;
    }

    public void touch() {
        this.lastActivity = LocalDateTime.now();
    }

    public boolean isSessionExpired() {
        if (currentUser == null || lastActivity == null) return false;
        return lastActivity.plusMinutes(AppConfig.getSessionTimeoutMinutes()).isBefore(LocalDateTime.now());
    }

    public UserDto getCurrentUser() {
        if (isSessionExpired()) {
            logout();
            return null;
        }
        touch();
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null && !isSessionExpired();
    }

    public UserRole getRole() {
        UserDto user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    public boolean isAdmin() {
        return getRole() == UserRole.ADMIN;
    }

    public boolean isStaff() {
        return getRole() == UserRole.STAFF;
    }

    public boolean canAccess(String fxmlPath) {
        if (!isLoggedIn()) {
            return "/view/Login.fxml".equals(fxmlPath);
        }
        if (isAdmin()) {
            return true;
        }
        return STAFF_ROUTES.contains(fxmlPath);
    }

    public String getRoleDisplayName() {
        if (getRole() == null) {
            return "";
        }
        return getRole() == UserRole.STAFF ? "Cashier" : getRole().name();
    }
}
