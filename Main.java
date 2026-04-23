package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static boolean isLoggedIn = false; // replace with your real login state

    @Override
    public void start(Stage primaryStage) {
        try {
            // Check database connection on startup
            DatabaseHandler.checkConnection();

            // Corrected path: use leading slash for root-based path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/home.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            if (getClass().getResource("/application/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/application/style.css").toExternalForm());
            }

            primaryStage.setTitle("Online Library Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
