package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;

    public void initialize() {
        updateNavButtons();
    }

    private void updateNavButtons() {
        boolean loggedIn = SessionManager.getCurrentUser() != null;
        loginButton.setVisible(!loggedIn);
        loginButton.setManaged(!loggedIn);
        signupButton.setVisible(!loggedIn);
        signupButton.setManaged(!loggedIn);

        profileButton.setVisible(loggedIn);
        profileButton.setManaged(loggedIn);
        logoutButton.setVisible(loggedIn);
        logoutButton.setManaged(loggedIn);
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        switchTo("login.fxml", "Login");
    }

    @FXML
    protected void handleSignUp(ActionEvent event) {
        switchTo("signup.fxml", "Sign Up");
    }

    @FXML
    protected void handleBooks(ActionEvent event) {
        if (SessionManager.getCurrentUser() == null) {
            showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Access Denied", "Please login to access books.");
            switchTo("login.fxml", "Login");
        } else {
            switchTo("books.fxml", "📚 Books Catalog");
        }
    }

    @FXML
    protected void handleProfile(ActionEvent event) {
        switchTo("profile.fxml", "👤 Profile");
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        SessionManager.logout();
        updateNavButtons();
        showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Logged Out",
                "You have been successfully logged out.");
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setMinHeight(500); // Prevent shrinking too much
            stage.setMinWidth(400);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}