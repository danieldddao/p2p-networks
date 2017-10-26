package chord;

import chord.Components.Node;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Main extends Application {

    public void showMainScene(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Book Sharing - Chord network");
        primaryStage.setScene(new Scene(root, 700, 500));
    }

    private void showLoadingScene(Stage primaryStage, String loadingText) throws Exception {
        BorderPane mainBorderPane = new BorderPane();

        Label label = new Label(loadingText);
        Pane pane = new Pane(label);
        label.layoutXProperty().bind(pane.widthProperty().subtract(label.widthProperty()).divide(2));
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        mainBorderPane.setTop(pane);
        mainBorderPane.setCenter(progressIndicator);

        Scene scene = new Scene(mainBorderPane, 300, 100);
        primaryStage.setScene(scene);
    }

    private void createNetworkButtonClicked(ActionEvent event, Stage primaryStage, Label alertLabel) {
        try {

            // Create new Chord network
            showLoadingScene(primaryStage, "Creating Network...");

            boolean status = Controller.getMyNode().createNewNetwork();
            if (status) {
                // Show the main window scene
                showMainScene(primaryStage);
            } else {
                alertLabel.setText("Unable to create new network! Please try again!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinNetworkButtonClicked(ActionEvent event, Stage primaryStage, TextField ipTextField, Label alertLabel) {
        try {
            // Check if given host address exists
            InetSocketAddress address = Node.getSocketAddressFrom(ipTextField.getText());

            if (address == null) {
                alertLabel.setText("Cannot find the host address you are trying to join! Please try again!");
            } else {
                showLoadingScene(primaryStage, "Joining Network...");

                // In a Thread:
                // Contact the given host address to join the network
                boolean status = Controller.getMyNode().joinNetwork(address);
                if (status) {
                    // Show the main window scene
                    showMainScene(primaryStage);
                } else {
                    alertLabel.setText("Unable to contact the host address you are trying to join! Please try again!");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Scene networkMenuScene(Stage primaryStage) throws Exception {
        BorderPane mainBorderPane = new BorderPane();
        BorderPane borderPane = new BorderPane();

        Label alertLabel = new Label();
        Pane pane = new Pane(alertLabel);
        pane.setMinHeight(20);
        alertLabel.setTextFill(Color.web("red"));
        alertLabel.layoutXProperty().bind(pane.widthProperty().subtract(alertLabel.widthProperty()).divide(2));

        // Pane to create new network
        BorderPane createNetworkPane = new BorderPane();
        Button createNetworkBtn = new Button("Create new network");
        createNetworkBtn.setOnAction(e-> createNetworkButtonClicked(e, primaryStage, alertLabel));
        createNetworkPane.setTop(new Label("    Create new Chord network:\n "));
        createNetworkPane.setLeft(new Label(""));
        createNetworkPane.setRight(createNetworkBtn);

        // Pane to join an existing network
        BorderPane joinNetworkPane = new BorderPane();
        Label joinNetworkLabel = new Label("Enter host address to join existing network:     \n(e.g. 127.0.0.1:8080)");
        TextField joinNetworkField = new TextField();
        Button joinNetworkBtn = new Button("Join");
        joinNetworkBtn.setOnAction(e -> joinNetworkButtonClicked(e, primaryStage, joinNetworkField, alertLabel));
        HBox joinNetworkHBox = new HBox(joinNetworkField, new Label(" "), joinNetworkBtn);
        joinNetworkPane.setTop(joinNetworkLabel);
        joinNetworkPane.setCenter(joinNetworkHBox);

        borderPane.setLeft(createNetworkPane);
        borderPane.setCenter(new Pane());
        borderPane.setRight(joinNetworkPane);

        mainBorderPane.setTop(pane);
        mainBorderPane.setCenter(borderPane);

        Scene menuScene = new Scene(mainBorderPane, 600, 100);
        return menuScene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Create a server socket
        Controller.createSocketWhenAppStarts();

        // Scene to create or join existing network
        Scene menuScene = networkMenuScene(primaryStage);
        primaryStage.setTitle("Create or Join a Chord network");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            System.out.println("App is closing");

            // Stop ListenerSocket Thread
            Controller.stopLoopThreads();

            // Clear registered books

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