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

public class MessagesController {

    @FXML
    private TableView<Message> messagesTable;
    @FXML
    private TableColumn<Message, String> senderColumn;
    @FXML
    private TableColumn<Message, String> messageColumn;
    @FXML
    private TableColumn<Message, String> timeColumn;
    @FXML
    private TextField recipientField;
    @FXML
    private TextArea messageArea;

    private ObservableList<Message> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            navigateTo("login.fxml", "Login");
            return;
        }

        senderColumn.setCellValueFactory(new PropertyValueFactory<>("senderName"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Load inbox by default
        loadMessages(user.getId());

        // Add listener for selection
        messagesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // If we click a message, we want to Reply to that person.
                // The message object has senderName, but we need the User object/username.
                // Or at least set the recipient field.
                // IMPORTANT: The message object senderName is "First Last", not username.
                // This is a flaw in current Message object design which complicates setting
                // recipient by username.
                // We should probably change Message to store senderUsername or fetch it.

                // Hack: We don't have username directly in Message.java constructor used in
                // DBHandler.
                // Check DatabaseHandler.java getMessagesForUser.
                // It does `rs.getString("firstname") + " " + rs.getString("lastname")`.
                // It does NOT fetch username.
                // I should probably fix DatabaseHandler to fetch username too, but I can't
                // easily change Message.java signatures everywhere without breaking things.
                // Or I can try to find user by name? No, risky.
                // Let's rely on sender_id. `id` field in Message is message id. `sender_id` is
                // stored.

                int senderId = newVal.getSenderId();
                if (senderId == user.getId()) {
                    // Start conversation with RECEIVER
                    int receiverId = newVal.getReceiverId();
                    User u = DatabaseHandler.getUserById(receiverId);
                    if (u != null)
                        setTargetUser(u);
                } else {
                    // Start conversation with SENDER
                    User u = DatabaseHandler.getUserById(senderId);
                    if (u != null)
                        setTargetUser(u);
                }
            }
        });
    }

    private void loadMessages(int userId) {
        data.setAll(DatabaseHandler.getMessagesForUser(userId));
        messagesTable.setItems(data);
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String recipientName = recipientField.getText().trim();
        String content = messageArea.getText().trim();

        if (recipientName.isEmpty() || content.isEmpty()) {
            showAlert("Warning", "Recipient and Message cannot be empty.");
            return;
        }

        User recipient = DatabaseHandler.getUser(recipientName);
        if (recipient == null) {
            showAlert("Error", "User '" + recipientName + "' not found.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        if (DatabaseHandler.sendMessage(currentUser.getId(), recipient.getId(), content)) {
            // showAlert("Success", "Message sent!"); // Optional: Don't show alert for
            // chat-like feel?
            messageArea.clear();
            // Refresh conversation if we are currently viewing this user
            if (currentStateUser != null && currentStateUser.getId() == recipient.getId()) {
                loadConversation(currentUser.getId(), recipient.getId());
            } else {
                // Or just reload inbox?
                // loadMessages(currentUser.getId());
            }
        } else {
            showAlert("Error", "Failed to send message.");
        }
    }

    private User currentStateUser;

    public void setTargetUser(User user) {
        this.currentStateUser = user;
        if (recipientField != null) {
            recipientField.setText(user.getUsername());
        }
        // Load conversation
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            loadConversation(currentUser.getId(), user.getId());
        }
    }

    private void loadConversation(int userId1, int userId2) {
        data.setAll(DatabaseHandler.getConversation(userId1, userId2));
        messagesTable.setItems(data);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        User user = SessionManager.getCurrentUser();
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            navigateTo("admin-dashboard.fxml", "Admin Dashboard");
        } else {
            navigateTo("client-dashboard.fxml", "Client Dashboard");
        }
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
            Stage stage = (Stage) messagesTable.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
