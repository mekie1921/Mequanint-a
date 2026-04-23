package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class BooksController {

    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, Boolean> selectColumn;
    @FXML
    private TableColumn<Book, String> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> genreColumn;
    @FXML
    private TableColumn<Book, String> editionColumn;
    @FXML
    private TableColumn<Book, String> yearColumn;
    @FXML
    private CheckBox selectAllCheckbox;
    @FXML
    private TextField searchField;

    private ObservableList<Book> masterData = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<Book> filteredData;

    @FXML
    private Button addBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button borrowBtn;

    @FXML
    public void initialize() {
        // Security Check
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            javafx.application.Platform.runLater(() -> {
                showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Access Denied",
                        "You must be logged in to view books.");
                navigateTo("login.fxml", "Login");
            });
            return;
        }

        // Role Layout
        if ("CLIENT".equalsIgnoreCase(user.getRole())) {
            addBtn.setVisible(false);
            addBtn.setManaged(false);
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
            borrowBtn.setVisible(true);
            borrowBtn.setManaged(true);
        } else {
            // Admin
            borrowBtn.setVisible(false);
            borrowBtn.setManaged(false);
        }

        // Bind columns to model properties
        selectColumn.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        editionColumn.setCellValueFactory(new PropertyValueFactory<>("edition"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        loadBooks();

        // Select All toggle - Only selects VISIBLE items
        selectAllCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            for (Book book : booksTable.getItems()) {
                book.setSelected(newVal);
            }
        });

        // Add listener to search field for real-time search (optional but good UX)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks(newValue);
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        filterBooks(searchField.getText());
    }

    private void filterBooks(String searchText) {
        if (filteredData == null)
            return;

        filteredData.setPredicate(book -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();

            if (book.getTitle().toLowerCase().contains(lowerCaseFilter))
                return true;
            if (book.getAuthor().toLowerCase().contains(lowerCaseFilter))
                return true;
            if (book.getGenre().toLowerCase().contains(lowerCaseFilter))
                return true;
            if (book.getId().toLowerCase().contains(lowerCaseFilter))
                return true;
            return false;
        });
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show(); // show() instead of showAndWait() to avoid blocking init if called there,
                      // but inside runLater showAndWait is safer if we want to blocking-wait before
                      // switch.
                      // Actually navigateTo creates a new scene, so the alert might disappear if we
                      // switch immediately.
                      // We'll let alert show, then navigate.
    }

    private void loadBooks() {
        masterData.setAll(DatabaseHandler.getAllBooks());
        filteredData = new javafx.collections.transformation.FilteredList<>(masterData, p -> true);
        booksTable.setItems(filteredData);
        booksTable.setEditable(true); // ✅ Enable editing so checkboxes work
    }

    @FXML
    private void handleBorrow(ActionEvent event) {
        ObservableList<Book> selected = FXCollections.observableArrayList();
        for (Book book : masterData) {
            if (book.isSelected())
                selected.add(book);
        }

        if (selected.isEmpty()) {
            showAlert(javafx.scene.control.Alert.AlertType.WARNING, "No Selection", "Please select a book to borrow.");
            return;
        }

        User user = SessionManager.getCurrentUser();
        int successCount = 0;
        for (Book book : selected) {
            if (DatabaseHandler.borrowBook(user.getId(), book.getId())) {
                successCount++;
            }
        }

        if (successCount > 0) {
            showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Success",
                    "Borrowed " + successCount + " book(s)!");
            // Deselect
            selectAllCheckbox.setSelected(false);
            for (Book b : masterData)
                b.setSelected(false);
        } else {
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Could not borrow books.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        User user = SessionManager.getCurrentUser();
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            navigateTo("admin-dashboard.fxml", "Admin Dashboard");
        } else if (user != null && "CLIENT".equalsIgnoreCase(user.getRole())) {
            navigateTo("client-dashboard.fxml", "Client Dashboard");
        } else {
            navigateTo("home.fxml", "Online Library");
        }
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        navigateTo("profile.fxml", "👤 Profile");
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        navigateTo("add-book.fxml", "➕ Add Book");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        ObservableList<Book> selected = FXCollections.observableArrayList();
        // Iterate over MASTER data to find all selected books (even those currently
        // hidden by filter)
        // Or should we only delete visible ones?
        // User said: "we can select one or more books on check box"
        // Usually, if I check a book, then search for something else, the check
        // remains.
        // I should probably delete ALL selected books.

        for (Book book : masterData) {
            if (book.isSelected()) {
                selected.add(book);
            }
        }

        if (selected.isEmpty()) {
            return;
        }

        for (Book book : selected) {
            DatabaseHandler.deleteBook(book.getId());
        }

        masterData.removeAll(selected);
        selectAllCheckbox.setSelected(false);
        System.out.println("Deleted " + selected.size() + " book(s)");
    }

    @FXML
    private void handleSelectAll(ActionEvent event) {
        // Handled via listener, but kept for FXML compliance
    }

    // Helper
    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) booksTable.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}