package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    @FXML
    private Button backBtn;

    @FXML
    private javafx.scene.control.ComboBox<String> roleSelector;

    @FXML
    public void initialize() {
        if (roleSelector != null) {
            roleSelector.getItems().addAll("Client", "Admin");
            roleSelector.setValue("Client");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleSelector != null ? roleSelector.getValue() : "Client";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Login Failed",
                    "Please enter both username and password.");
            return;
        }

        // Validate Login against specific table based on Role
        if (DatabaseHandler.validateLogin(username, password, selectedRole)) {
            // Fetch User details (now we know context, but getUser searches both)
            User currentUser = DatabaseHandler.getUser(username);

            if (currentUser != null) {
                // Secondary fallback check (should be redundant if validateLogin works)
                if (selectedRole != null && !selectedRole.equalsIgnoreCase(currentUser.getRole())) {
                    showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Access Denied",
                            "Account seems to exist but role mismatch.");
                    return;
                }

                SessionManager.setCurrentUser(currentUser);
                System.out.println("✅ Login successful! Welcome, " + currentUser.getFirstName());

                if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                    navigateTo("admin-dashboard.fxml", "Admin Dashboard");
                } else {
                    navigateTo("client-dashboard.fxml", "Client Dashboard");
                }
            } else {
                showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Login Failed", "Error fetching user details.");
            }
        } else {
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Login Failed",
                    "Invalid credentials for " + selectedRole + " role.");
        }
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack(ActionEvent event) {
        navigateTo("home.fxml", "Online Library Management System");
    }

    // ✅ Added: Go to Signup
    @FXML
    private void goToSignup(javafx.scene.input.MouseEvent event) {
        navigateTo("signup.fxml", "Sign Up");
    }

    // Helper
    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            stage.setMinWidth(400);
            stage.setMinHeight(500); // ⬅️ Ensures footer is always visible

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}