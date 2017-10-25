package chord;

import chord.Components.Node;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.*;
import java.net.*;

public class Controller {

    private static Node myNode = null;
    public static Node getMyNode() {
        return myNode;
    }

    public static void stopLoopThreads() {
        myNode.stopLoopThreads();
    }

    /**
     * Check if given address is available
     * @param address
     * @return true if given address is available, false otherwise.
     */
    public static boolean available(InetSocketAddress address) {
//        try (Socket ignored = new Socket(address.getAddress(), address.getPort())) {
        Socket ignored = new Socket();
        try {
            ignored.connect(address, 1000);
            // Send message to check port availability
            // Send message to the server
            OutputStream outputStream = ignored.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println("checking if port available");

            System.out.println("port #" + address.getPort() + " not available");

            printStream.close();
            outputStream.close();
            ignored.close();
            return false;
        } catch (SocketTimeoutException e) {
            System.out.println("Cannot connect to address");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("port #" + address.getPort() + " available");
            return true;
        }
    }

    /**
     * Find available port, create server socket, and initialize Chord node
     */
    public static void createSocketWhenAppStarts() {
        try {
            int initialPort = 1111;
            InetSocketAddress myAddress = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);
            while (!available(myAddress)) {
                initialPort += 1;
                System.out.println("Checking port #" + initialPort);
                myAddress = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);
            }
            Controller.myNode = new Node(myAddress);
            System.out.println("Socket created on " + myAddress.getAddress().getHostAddress() + ":" + myAddress.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Label addressLabel;

    /**
     * Initialize stuff when main starts
     */
    public void initialize() {
        try {
            InetSocketAddress address = Controller.myNode.getHostAddress();
            addressLabel.setText("My host address: " + address.getAddress().getHostAddress() + ":" + address.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Search a book tab
     **/

    @FXML
    private TextField searchTextField;

    @FXML
    private Text searchAlertText;

    @FXML
    private ListView<Book> bookListView;

    public void searchABookTabSelected(Event event) {
        searchAlertText.setText("");
        ObservableList<Book> list = FXCollections.observableArrayList();
        bookListView.setItems(list);
    }

    public void searchBook(ActionEvent event) {

    }



    /**
     * Share a book tab
     **/

    @FXML
    private Text chooseFileText;

    @FXML
    private Text alertText;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField authorTextField;

    @FXML
    private TextField isbnTextField;

    private File selectedFile;

    public void shareABookTabSelected(Event event) {
        alertText.setText("");
    }

    public void chooseFileButtonSelected(ActionEvent event) {

    }

    public void shareNewBook(ActionEvent event) {

    }



    /**
     * My shared books tab
     **/

    @FXML
    private ListView<Book> mySharedBooksListView;

    public void mySharedBooksTabSelected(Event event) {

    }

}
