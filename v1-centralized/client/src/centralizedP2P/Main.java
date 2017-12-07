package centralizedP2P;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import centralizedP2P.RunnableThread.Listener;

public class Main extends Application {

    private void showMainScene(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("Book Sharing - Centralized v1.0");
        primaryStage.setScene(new Scene(root, 700, 500));
    }

    private void connectServerButtonClicked(ActionEvent event, Stage primaryStage, TextField serverTextField, Label alertLabel) {
        try {

            // Check if server is available to connect and create server sockets
            boolean success = AppController.connectToServer(serverTextField.getText());
            if (success) {
                showMainScene(primaryStage);
            } else {
                alertLabel.setText("Unable to connect to server! Server address might be incorrect!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Scene networkMenuScene(Stage primaryStage) throws Exception {
        BorderPane mainBorderPane = new BorderPane();

        Label alertLabel = new Label();
        Pane pane = new Pane(alertLabel);
        pane.setMinHeight(20);
        alertLabel.setTextFill(Color.web("red"));
        alertLabel.layoutXProperty().bind(pane.widthProperty().subtract(alertLabel.widthProperty()).divide(2));

        // Pane to connect to a server
        BorderPane connectServerPane = new BorderPane();
        Label connectServerLabel = new Label("Enter server address to connect to the central server:     \n(e.g. 127.0.0.1:8080)\n(Default server: p2p-centralized-server.herokuapp.com)");
        TextField connectServerField = new TextField();
        Button connectServerBtn = new Button("Connect");
        connectServerBtn.setOnAction(e -> connectServerButtonClicked(e, primaryStage, connectServerField, alertLabel));
        HBox joinNetworkHBox = new HBox(connectServerField, new Label(" "), connectServerBtn);
        connectServerPane.setTop(connectServerLabel);
        connectServerPane.setCenter(joinNetworkHBox);

        mainBorderPane.setTop(pane);
        mainBorderPane.setCenter(connectServerPane);
        mainBorderPane.setLeft(new Label(" "));

        Scene menuScene = new Scene(mainBorderPane, 400, 100);
        return menuScene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Scene to join a central server
        Scene menuScene = networkMenuScene(primaryStage);
        primaryStage.setTitle("Centralized-P2P - Connect to server");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            System.out.println("App is closing");
            // Clear registered books from the server
            WebServer.unshareBooksFromServerWhenExiting(AppController.getUsernameString());

            Listener.closeListener();
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
