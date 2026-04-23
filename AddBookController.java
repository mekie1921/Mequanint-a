package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AddBookController {

    @FXML
    private Button backButton;
    @FXML
    private TextField bookIdField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField editionField;
    @FXML
    private TextField yearField;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            javafx.application.Platform.runLater(() -> {
                showAlert("Access Denied", "Please login to add books.");
                navigateTo("login.fxml", "Login");
            });
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("books.fxml", "📚 Books Catalog");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String bookId = bookIdField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre = genreField.getText().trim();
        String year = yearField.getText().trim();

        // ✅ Validate required fields
        if (bookId.isEmpty()) {
            showAlert("⚠️ Missing", "Book ID is required.");
            return;
        }
        if (title.isEmpty()) {
            showAlert("⚠️ Missing", "Title is required.");
            return;
        }
        if (author.isEmpty()) {
            showAlert("⚠️ Missing", "Author is required.");
            return;
        }
        if (genre.isEmpty()) {
            showAlert("⚠️ Missing", "Genre is required.");
            return;
        }
        if (year.isEmpty()) {
            showAlert("⚠️ Missing", "Year is required.");
            return;
        }

        // Optional: validate year is numeric
        try {
            Integer.parseInt(year);
        } catch (NumberFormatException e) {
            showAlert("⚠️ Invalid", "Year must be a number (e.g., 1925).");
            return;
        }

        if (DatabaseHandler.addBook(bookId, title, author, genre,
                editionField.getText().trim(), year)) {
            System.out.println("✅ Book added to database!");
            navigateTo("books.fxml", "📚 Books Catalog");
        } else {
            showAlert("Error", "Failed to add book. ID might be duplicate.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateTo("books.fxml", "📚 Books Catalog");
    }

    // --- Helpers ---
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