package chord;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load shared books and create server sockets

        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Free Book Sharing");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();

        MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
        String input = "test";
        msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
        String hash = msdDigest.digest();

        System.out.println(sha1);
    }

    @Override
    public void stop() {
        System.out.println("App is closing");

        // Clear registered books from the server
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}