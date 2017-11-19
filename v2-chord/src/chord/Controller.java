package chord;

import chord.Components.Book;
import chord.Components.MessageType;
import chord.Components.Node;

import chord.Components.Utils;
import chord.Runnable.DownloadClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.*;
import java.util.List;

public class Controller {

    private static Node myNode = null;
    public static Node getMyNode() {
        return myNode;
    }
    public static void setMyNode(Node n) {myNode = n; }

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
            System.out.println("Connecting to " + address.getAddress().getHostAddress() + ":" + address.getPort());
            ignored.connect(address, 1000);
            // Send message to server to check port availability
            Object[] objArray = new Object[1];
            objArray[0] = MessageType.CHECKING_IF_PORT_IS_AVAILABLE;
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(ignored.getOutputStream());
            objectOutputStream.writeObject(objArray);
            objectOutputStream.flush();

            // Receive response message
            ObjectInputStream objectInputStream = new ObjectInputStream(ignored.getInputStream());
            objectInputStream.readObject();

            objectInputStream.close();
            objectOutputStream.close();
            ignored.close();

            System.out.println("port #" + address.getPort() + " not available");
            return false;
        } catch (SocketTimeoutException e ) {
            System.out.println("port #" + address.getPort() + " available");
            return true;
        } catch (ConnectException e ) {
            System.out.println("port #" + address.getPort() + " available");
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("port #" + address.getPort() + " available");
            return true;
        }
    }

    private static InetAddress getLocalHost() {
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            return address;
//            return InetAddress.getLocalHost();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find available port, create server socket, and initialize Chord node
     */
    public static void createSocketWhenAppStarts() {
        try {
            int initialPort = 1111;
            InetSocketAddress myAddress = new InetSocketAddress(getLocalHost(), initialPort);
            while (!available(myAddress)) {
                initialPort += 1;
//                System.out.println("Checking port #" + initialPort);
                myAddress = new InetSocketAddress(getLocalHost(), initialPort);
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
            InetSocketAddress address = Controller.myNode.getAddress();
            addressLabel.setText("My address: " + address.getAddress().getHostAddress() + ":" + address.getPort());
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

    class BookCell extends ListCell<Book> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Button downloadButton = new Button("Download");
        BorderPane borderPane = new BorderPane();
        CheckBox checkBox = new CheckBox();
        BorderPane progressBorderPane = new BorderPane();
        ProgressBar progressBar = new ProgressBar(0);
        Book currentBook;

        public BookCell() {
            super();
            downloadButton.setDisable(false);
            borderPane.setTop(downloadButton);
            borderPane.setCenter(new Label(""));
            borderPane.setBottom(progressBorderPane);

            progressBorderPane.setLeft(progressBar);
            progressBorderPane.setCenter(new Label(" "));
            progressBorderPane.setRight(checkBox);

            hbox.getChildren().addAll(label, pane, borderPane);
            HBox.setHgrow(pane, Priority.ALWAYS);

            // Download Button is pressed
            downloadButton.setOnAction(e -> {
                // Choose directory
                DirectoryChooser dc = new DirectoryChooser();
                File dir = dc.showDialog(null);
                if (dir != null) {
                    try {
                        System.out.println("directory selected: " + dir);
                        progressBar.setProgress(0);

                        // Ask book owner if book is still available
                        Object[] objArray = new Object[2];
                        objArray[0] = MessageType.IS_BOOK_AVAILABLE;
                        objArray[1] = currentBook.getLocation();
                        MessageType response = (MessageType) Utils.sendMessage(currentBook.getOwnerAddress(), objArray);
                        if (response == MessageType.BOOK_IS_AVAILABLE) {
                            //creating connection to owner's socket
                            Socket socket = new Socket(currentBook.getOwnerAddress().getAddress(), currentBook.getOwnerAddress().getPort());
                            System.out.println("Connecting to book owner: " + currentBook.getOwnerAddress().getAddress().getHostAddress() + ":" + currentBook.getOwnerAddress().getPort());

                            Thread t = new Thread(new DownloadClient(socket, currentBook, dir, progressBar, checkBox));
                            t.start();
                            System.out.println("Downloading " + currentBook.getTitle() + " from loc: " + currentBook.getLocation());
                        } else {
                            searchAlertText.setText("Book is no longer available to download from this user!");

                            // Make the download button disabled
                            downloadButton.setDisable(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    searchAlertText.setText("Please choose a directory to download");
                }
            });
        }

        @Override
        protected void updateItem(Book newBook, boolean empty) {
            super.updateItem(newBook, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                currentBook = null;
                setGraphic(null);
            } else {
                currentBook = newBook;
                if (currentBook.getIsbn().isEmpty()) {
                    label.setText(currentBook.getTitle() + " by " + currentBook.getAuthor());
                } else {
                    label.setText(currentBook.getTitle() + " by " + currentBook.getAuthor() + " (isbn:" + currentBook.getIsbn() + ")");
                }
                checkBox.setSelected(true);
                checkBox.setDisable(true);
                checkBox.setVisible(false);
                checkBox.setText("Done");
                setGraphic(hbox);
            }
        }
    }

    public void searchBook(ActionEvent event) {
        try {
            searchAlertText.setText("");
            List<Book> searchBookResult = myNode.searchBook(searchTextField.getText());
            if (searchBookResult.isEmpty()) {
                searchAlertText.setText("No Book found!");
            } else {
                ObservableList<Book> list = FXCollections.observableArrayList();
                list.addAll(searchBookResult);
                bookListView.setItems(list);
                bookListView.setCellFactory(param -> new BookCell());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        FileChooser fc = new FileChooser();
        selectedFile = fc.showOpenDialog(null);

        if (selectedFile != null) {
            chooseFileText.setText("File selected: " + selectedFile);
        } else {
            alertText.setText("No file selected");
        }
    }

    public void shareNewBook(ActionEvent event) {
        System.out.println("Save button pressed");
        alertText.setText("");
        try {
            if (titleTextField.getText().isEmpty()) {
                alertText.setText("Title can't be empty");
            } else if (authorTextField.getText().isEmpty()) {
                alertText.setText("Author can't be empty");
            } else if (selectedFile == null) {
                alertText.setText("Please choose a file");
            } else if (!selectedFile.exists()) {
                alertText.setText("Selected file doesn't exist");
            } else {
                // Share a new book with the network
                boolean status = Controller.getMyNode().shareABook(titleTextField.getText(), authorTextField.getText(), isbnTextField.getText(), selectedFile.toString());
                if (status) {
                    alertText.setText("New Book '" + titleTextField.getText() + "' successfully shared");
                } else {
                    alertText.setText("Can't share book! Please try again!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * My shared books tab
     **/

    @FXML
    private ListView<Book> mySharedBooksListView;

    public void mySharedBooksTabSelected(Event event) {
        // Load books in the local database
    }

}

