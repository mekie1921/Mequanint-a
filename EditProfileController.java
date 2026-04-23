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

public class EditProfileController {

    @FXML
    private Button backButton;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    // ✅ Simulated current user data (will come from DB later)

    @FXML
    public void initialize() {
        // Security Check
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            javafx.application.Platform.runLater(() -> {
                showAlert("Access Denied", "Please login.");
                navigateTo("login.fxml", "Login");
            });
            return;
        }

        // Load current data into fields
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        usernameField.setText(user.getUsername());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String newPassword = passwordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
            showAlert("⚠️ Required", "First Name, Last Name, and Username are required.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in.");
            return;
        }

        // Update local object
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setUsername(username);
        if (!newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
        }

        // Save to DB
        if (DatabaseHandler.updateUser(currentUser)) {
            System.out.println("✅ Profile updated for: " + username);
            navigateTo("profile.fxml", "👤 Your Profile");
        } else {
            showAlert("Error", "Failed to update profile.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateTo("profile.fxml", "👤 Your Profile");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("profile.fxml", "👤 Your Profile");
    }

    // Helper
    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setTitle(title);
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