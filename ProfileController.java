package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;

    @FXML
    private ImageView profileImage;
    @FXML
    private Button backButton;
    @FXML
    private Button editButton;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            firstNameLabel.setText(user.getFirstName());
            lastNameLabel.setText(user.getLastName());
            usernameLabel.setText(user.getUsername());
            passwordLabel.setText("••••••••"); // Don't show plain password
        } else {
            // Should not happen if access is controlled, but just in case
            javafx.application.Platform.runLater(() -> {
                showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Access Denied", "Please login.");
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
                    Stage stage = (Stage) backButton.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 600));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Go back - usually to Home or Books. Home is safer landing.
        navigateTo("home.fxml", "Online Library");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        navigateTo("edit-profile.fxml", "✏️ Edit Profile");
    }

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

    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}