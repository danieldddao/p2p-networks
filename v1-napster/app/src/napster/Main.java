package napster;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import napster.Runnable.ServerSocketRunnable;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load shared books and create server sockets
        AppController.loadSharedBookWhenAppStarts();

        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("Free Book Sharing");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            System.out.println("App is closing");
            // Clear registered books from the server
            WebServer.unshareBooksFromServerWhenExiting(AppController.getUsernameString());

            ServerSocketRunnable.closeServerSocket();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
