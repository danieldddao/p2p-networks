package napster;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.ServerSocket;
import java.util.List;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load shared books and create server sockets
        Book.loadSharedBookWhenAppStarts();

        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("Free Book Sharing");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("App is closing");
        // Clear registered books from the server
        WebServer.unshareBooksFromServerWhenExiting();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
