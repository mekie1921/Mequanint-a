package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class MyBooksController {

    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, String> bookTitleColumn;
    @FXML
    private TableColumn<Transaction, String> borrowDateColumn;
    @FXML
    private TableColumn<Transaction, String> returnDateColumn;
    @FXML
    private TableColumn<Transaction, String> statusColumn;
    @FXML
    private Button returnBtn;

    private ObservableList<Transaction> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            navigateTo("login.fxml", "Login");
            return;
        }

        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTransactions(user.getId());
    }

    private void loadTransactions(int userId) {
        data.setAll(DatabaseHandler.getUserTransactions(userId));
        transactionsTable.setItems(data);
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a book to return.");
            return;
        }

        if ("RETURNED".equalsIgnoreCase(selected.getStatus())) {
            showAlert("Info", "This book is already returned.");
            return;
        }

        if (DatabaseHandler.returnBook(selected.getId())) {
            showAlert("Success", "Book returned successfully!");
            loadTransactions(SessionManager.getCurrentUser().getId());
        } else {
            showAlert("Error", "Could not return book.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("client-dashboard.fxml", "Client Dashboard");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) transactionsTable.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
