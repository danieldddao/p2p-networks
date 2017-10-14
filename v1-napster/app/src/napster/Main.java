package napster;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SQLiteDB.connectToDB();
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("Free Book Sharing");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    @Override
    public void stop(){
        System.out.println("App is closing");
        // Clear registered books from the server
        WebServer.clearBooksFromServerWhenExiting();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
