package chord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;

public class Controller {

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

    private int initialPort = 1111;

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

