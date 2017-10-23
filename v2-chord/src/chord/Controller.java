package chord;

import chord.Components.Node;
import chord.Runnable.ServerSocketRunnable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.*;

public class Controller {

    private static Node myNode = null;
    public static Node getMyNode() {
        return myNode;
    }

    /**
     * Check if given address is available
     * @param address
     * @return true if given address is available, false otherwise.
     */
    public static boolean available(InetSocketAddress address) {
        try (Socket ignored = new Socket(address.getAddress(), address.getPort())) {
            System.out.println("port #" + address.getPort() + " not available");
            // Send message to check port availability
            ObjectOutputStream out = new ObjectOutputStream(ignored.getOutputStream());
            out.writeObject("checking if port available");
            out.flush();
            out.close();
            ignored.close();
            return false;
        } catch (ConnectException ignored) {
            System.out.println("port #" + address.getPort() + " available");
            return true;
        }
        catch ( Exception e) {
            return false;
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
                myAddress = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);
            }

            ServerSocket serverSocket = new ServerSocket(myAddress.getPort(), 0,myAddress.getAddress());
            Thread t = new Thread(new ServerSocketRunnable(serverSocket));
            t.start();
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

