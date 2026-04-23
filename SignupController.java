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

public class SignupController {

    @FXML
    private TextField fname;
    @FXML
    private TextField lname;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button backBtn;
    @FXML
    private Button signupBtn;

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
    private void handleSignup(ActionEvent event) {
        String firstName = fname.getText().trim();
        String lastName = lname.getText().trim();
        String usernames = username.getText().trim();
        String passwords = password.getText();
        String role = roleSelector.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || usernames.isEmpty() || passwords.isEmpty()) {
            showAlert("⚠️ Incomplete", "All fields are required.");
            return;
        }

        if (passwords.length() < 6) {
            showAlert("⚠️ Weak Password", "Password must be at least 6 characters.");
            return;
        }

        if (DatabaseHandler.checkUsernameExists(usernames)) {
            showAlert("⚠️ User Exists", "Username already taken.");
            return;
        }

        // Convert UI role to DB constant if needed (Admin -> ADMIN)
        String dbRole = role.toUpperCase();

        if (DatabaseHandler.registerUser(firstName, lastName, usernames, passwords, dbRole)) {
            showAlert("Success", "Account created as " + role + "! Please login.");
            goToLogin();
        } else {
            String dbError = DatabaseHandler.getLastError();
            if (dbError != null && !dbError.isEmpty()) {
                showAlert("Error", "Signup failed: " + dbError);
            } else {
                showAlert("Error", "Signup failed. Please try again.");
            }
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        navigateTo("home.fxml", "Online Library Management System");
    }

    @FXML
    private void goToLogin() {
        navigateTo("login.fxml", "Login");
    }

    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600); // ⬅️ Fixed height ensures visibility
            // ... rest unchanged ...
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMinHeight(500); // Prevent shrinking too much
            stage.setMinWidth(400);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}