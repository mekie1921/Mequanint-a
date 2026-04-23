package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        navigateTo("login.fxml", "Login");
    }

    @FXML
    private void handleBrowseBooks() {
        navigateTo("books.fxml", "Browse Books");
    }

    @FXML
    private void handleMyBooks() {
        navigateTo("my-books.fxml", "My Borrowed Books");
    }

    @FXML
    private void handleMessages() {
        navigateTo("messages.fxml", "Messages");
    }

    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
