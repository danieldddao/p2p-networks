package chord;

import chord.Runnable.ServerSocketRunnable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Controller {

    private static int initialPort = 1111;

    private static boolean available(int port) {
        try (Socket ignored = new Socket(InetAddress.getLocalHost().getHostAddress(), port)) {
            System.out.println(port + " not available");
            // Send message to check port availability
            ObjectOutputStream out = new ObjectOutputStream(ignored.getOutputStream());
            out.writeObject("checking if port available");
            out.flush();
            out.close();
            ignored.close();
            return false;
        } catch (ConnectException ignored) {
            System.out.println(port + " available");
            return true;
        }
        catch ( Exception e) {
            return false;
        }
    }

    public static void createSocketWhenAppStarts() {
        try {
            System.out.println("loading shared books");
            while (!available(initialPort)) {
                initialPort += 1;
            }

            ServerSocket serverSocket = new ServerSocket(initialPort, 0, InetAddress.getLocalHost());
            Thread t = new Thread(new ServerSocketRunnable(serverSocket));
            t.start();
            System.out.println("Socket created on port # " + serverSocket.getLocalSocketAddress());
            
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

