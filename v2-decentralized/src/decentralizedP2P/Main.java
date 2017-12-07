package decentralizedP2P;

import decentralizedP2P.Components.Node;

import decentralizedP2P.Components.Utils;
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

public class Main extends Application {

    public void showMainScene(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Book Sharing - Decentralized P2P v1.0");
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

    private void createNetworkButtonClicked(ActionEvent event, Stage primaryStage, TextField createNetworkField, Label alertLabel) {
        try {
            int networkSize = 7;
            if (!createNetworkField.getText().isEmpty()) {
                networkSize = Integer.parseInt(createNetworkField.getText());
            }
            if (networkSize < 2) {
                alertLabel.setText("m must be > 1");
            } else {
                // Create new Chord network
                showLoadingScene(primaryStage, "Creating Network...");

                Node.setM(networkSize);

                // Create a server socket
                Controller.createSocketWhenAppStarts();

                boolean status = Controller.getMyNode().createNewNetwork();
                if (status) {
                    // Show the main window scene
                    showMainScene(primaryStage);
                } else {
                    alertLabel.setText("Unable to create new network! Please try again!");
                }
            }
        } catch (IllegalArgumentException ex) {
            alertLabel.setText("Please enter correct size!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinNetworkButtonClicked(ActionEvent event, Stage primaryStage, TextField ipTextField, Label alertLabel) {
        try {
            // Create a server socket
            Controller.createSocketWhenAppStarts();

            // Check if given address exists
            InetSocketAddress address = Utils.checkAddressExist(ipTextField.getText());

            if (address == null) {
                alertLabel.setText("Cannot find the address you are trying to join! Please try again!");
            } else {
                showLoadingScene(primaryStage, "Joining Network...");

                // In a Thread:
                // Contact the given address to join the network
                boolean status = Controller.getMyNode().joinNetwork(address);
                if (status) {
                    // Show the main window scene
                    showMainScene(primaryStage);
                } else {
                    alertLabel.setText("Unable to contact the address you are trying to join! Please try again!");
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
        TextField createNetworkField = new TextField();
        createNetworkField.setPromptText("default m = 7");
        Button createNetworkBtn = new Button("Create");
        createNetworkBtn.setOnAction(e-> createNetworkButtonClicked(e, primaryStage, createNetworkField, alertLabel));
        createNetworkPane.setTop(new Label("    Enter size of the network (2^m): \n    (Default: m=7) m="));
        createNetworkPane.setLeft(createNetworkField);
        createNetworkPane.setCenter( new Label(" "));
        createNetworkPane.setRight(createNetworkBtn);

        // Pane to join an existing network
        BorderPane joinNetworkPane = new BorderPane();
        Label joinNetworkLabel = new Label("Enter address to join existing network:     \n(e.g. 127.0.0.1:8080)");
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

        // Scene to create or join existing network
        Scene menuScene = networkMenuScene(primaryStage);
        primaryStage.setTitle("Create or Join a decentralized P2P network - v1.0");
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