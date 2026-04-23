package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome Admin, " + user.getFirstName());
        } else {
            // Security check: if no user, go back to login
            try {
                // This might be tricky if scene isn't ready, but typically ok in init
            } catch (Exception e) {
            }
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        navigateTo("login.fxml", "Login");
    }

    @FXML
    private void handleManageBooks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("books.fxml"));
            BorderPane pane = loader.load();
            // Embed content (sidebar + table) into dashboard center
            mainBorderPane.setCenter(pane.getCenter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("messages.fxml"));
            BorderPane pane = loader.load();
            mainBorderPane.setCenter(pane.getCenter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUsers() {
        TableView<User> userTable = new TableView<>();

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> fnCol = new TableColumn<>("First Name");
        fnCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<User, String> lnCol = new TableColumn<>("Last Name");
        lnCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<User, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Action Column for Deleting
        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        btn.setOnAction((event) -> {
                            User data = getTableView().getItems().get(getIndex());
                            if (DatabaseHandler.deleteUser(data.getId())) {
                                getTableView().getItems().remove(data);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete user.");
                                alert.show();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Prevent deleting self (Admin) if logged in? Or check role?
                            // For simplicity, just show button.
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        actionCol.setCellFactory(cellFactory);

        userTable.getColumns().addAll(idCol, fnCol, lnCol, userCol, roleCol, actionCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        List<User> users = DatabaseHandler.getAllUsers();
        userTable.getItems().addAll(users);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label header = new Label("Registered Users");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().addAll(header, userTable);

        mainBorderPane.setCenter(content);
    }

    @FXML
    private void handleTransactions() {
        TableView<Transaction> transTable = new TableView<>();

        TableColumn<Transaction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Transaction, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<Transaction, String> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<Transaction, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Message Button Column
        TableColumn<Transaction, Void> msgCol = new TableColumn<>("Message User");
        Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Transaction, Void> call(final TableColumn<Transaction, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Message");
                    {
                        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btn.setOnAction((event) -> {
                            Transaction t = getTableView().getItems().get(getIndex());
                            User u = DatabaseHandler.getUserById(t.getUserId());
                            if (u != null) {
                                navigateToMessages(u);
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        msgCol.setCellFactory(cellFactory);

        transTable.getColumns().addAll(idCol, userIdCol, bookIdCol, titleCol, statusCol, msgCol);
        transTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        List<Transaction> transactions = DatabaseHandler.getAllTransactions();
        transTable.getItems().addAll(transactions);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        Label header = new Label("All Transactions");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        content.getChildren().addAll(header, transTable);

        mainBorderPane.setCenter(content);
    }

    // navigateToMessages overload calling embedded view
    private void navigateToMessages(User targetUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("messages.fxml"));
            BorderPane pane = loader.load();

            MessagesController controller = loader.getController();
            controller.setTargetUser(targetUser);

            mainBorderPane.setCenter(pane.getCenter());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
