package napster;

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
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.text.Text;

import java.io.*;
import java.net.*;
import java.util.List;

import napster.Runnable.ClientRunnable;
import napster.Runnable.ServerSocketRunnable;

public class AppController {

    private static InetSocketAddress myAddress = null;

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

    public static boolean loadSharedBookWhenAppStarts(String url) {
        try {
            WebServer.setUrl("http://" + url);

            int initialPort = 1111;
            System.out.println("loading shared books");
            while (!available(initialPort)) {
                initialPort += 1;
            }
            myAddress  = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);

            String mySharedBooks = WebServer.findAllMySharedBooks(usernameString);
            if (mySharedBooks == null) {return false;}
            System.out.println("my shared books: " + mySharedBooks);
            List<Book> bookList = Book.jsonToBookList(mySharedBooks);
            for (Book book: bookList) {
                System.out.println("Loading book " + book.getTitle() + " by " + book.getAuthor());
                File location = new File(book.getLocation());
                if (location.exists()) {
                    // update sharing status in the server
                    WebServer.updateBookSharingStatus(book, true);
                } else {
                    System.out.println("Book " + book.getTitle() + " by " + book.getAuthor() + " doesn't exist in the given location: " + book.getLocation());
                    WebServer.updateBookSharingStatus(book, false);
                }
            }

            ServerSocket serverSocket = new ServerSocket(myAddress.getPort(), 0, myAddress.getAddress());
            Thread t = new Thread(new ServerSocketRunnable(serverSocket));
            t.start();
            System.out.println("Socket created on port # " + serverSocket.getLocalSocketAddress());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

                        //creating connection to owner's socket
                        System.out.println("Connecting to port " + currentBook.getPort());
                        Socket socket = new Socket(currentBook.getUser_ip(), currentBook.getPort());
                        System.out.println("Socket connected to " + currentBook.getUser_ip() + " port: " + currentBook.getPort());

                        Thread t = new Thread(new ClientRunnable(socket, currentBook, dir, progressBar, checkBox));
                        t.start();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else {
                    searchAlertText.setText("Please choose a directory to download");
                }

                System.out.println("Downloading " + currentBook.getTitle() + " from loc: " + currentBook.getLocation());
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

    private void downloadBook() {
//        try {
//            File myFile = new File("/Users/danieldao/Downloads/jb1.ipa");
//            byte[] mybytearray = new byte[(int) myFile.length()];
//
//            FileInputStream fis = new FileInputStream(myFile);
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            bis.read(mybytearray, 0, mybytearray.length);
//
//            OutputStream os = sock.getOutputStream();
//
//            os.write(mybytearray, 0, mybytearray.length);
//
//            os.flush();
//
//            sock.close();
//            System.out.println("Received file!");
//        } catch (Exception e) {
//
//        }
    }

    public void searchBook(ActionEvent event) {
        try {
            searchAlertText.setText("");
            String searchBookResult = WebServer.searchBook(searchTextField.getText());
//            String searchBookResult = "[{\"id\":1,\"user_ip\":\"0.0.0.0/0.0.0.0\",\"port_number\":\"1111\",\"title\":\"EV\",\"isbn\":\"\",\"author\":\"dd\",\"location\":\"/Users/danieldao/Downloads/Evaluation form-3.docx\"},{\"id\":2,\"user_ip\":\"0.0.0.0/0.0.0.0\",\"port_number\":\"1112\",\"title\":\"jb\",\"isbn\":\"\",\"author\":\"dd\",\"location\":\"/Users/danieldao/Downloads/jb.ttc\"}]\n";
            if (searchBookResult.equals("[]") || searchBookResult.isEmpty()) {
                searchAlertText.setText("No Book found!");
            } else {
                ObservableList<Book> list = FXCollections.observableArrayList();
                for (Book book: Book.jsonToBookList(searchBookResult)) {
                    list.add(book);
                }
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

    /*
     * Login/Signup session
     */
    @FXML private ToggleButton loginToggle;

    @FXML private ToggleButton signupToggle;

    @FXML private TextField username;

    @FXML private PasswordField password;

    @FXML private Button lsButton;

    @FXML private Text lsTextAlert;

    @FXML private Text lsTitle;

    @FXML private Text loginText;

    private static String usernameString = "";

    public static String getUsernameString() {
        return usernameString;
    }

    public void loginToggledPressed(ActionEvent event) {
        signupToggle.setSelected(false);
        lsButton.setText("Login");
    }

    public void signupToggledPressed(ActionEvent event) {
        loginToggle.setSelected(false);
        lsButton.setText("Signup");
    }

    public void lsButtonPressed(ActionEvent event) {
        if (username.getText().isEmpty()) {
            lsTextAlert.setText("Please enter username");
        } else if (password.getText().isEmpty()) {
            lsTextAlert.setText("Please enter password");
        } else {
            // create new account
            if (lsButton.getText().equals("Signup")) {
                boolean status = WebServer.createNewUser(username.getText(), password.getText());
                if (status == true) {
                    lsTextAlert.setText("Successfully created new account!");
                    username.setText("");
                    password.setText("");
                } else {
                    lsTextAlert.setText("Error! Can't create account! Please try again!");
                }
            }
            // login to existing account
            else if (lsButton.getText().equals("Login")) {
                int response = WebServer.loginToServer(username.getText(), password.getText(), myAddress);
                switch (response) {
                    case 200:
                        // successfully logged in, hide buttons, show logout button
                        loginToggle.setVisible(false);
                        signupToggle.setVisible(false);
                        username.setVisible(false);
                        password.setVisible(false);
                        lsTitle.setVisible(false);
                        lsButton.setText("Logout");
                        usernameString = username.getText();
                        loginText.setText("You're logged in as " + usernameString);
                        lsTextAlert.setText("");

                        titleTextField.setDisable(false);
                        authorTextField.setDisable(false);
                        isbnTextField.setDisable(false);
                        addButton.setDisable(false);
                        chooseAFileButton.setDisable(false);
                        break;

                    case 204:
                        // username doesn't exist/password is not correct
                        lsTextAlert.setText("Username doesn't exist/Password is not correct");
                        break;

                    case 208:
                        // username already logged in
                        lsTextAlert.setText("User with this username, " + username.getText() + ", already logged in");
                        break;

                    default:
                        // username already logged in
                        lsTextAlert.setText("User with this username, " + username.getText() + ", already logged in");
                        break;
                }
            }
            // Log out
            else {
                boolean status = WebServer.logoutFromServer(usernameString);
                if (status) {
                    loginToggle.setVisible(true);
                    loginToggle.setSelected(true);
                    signupToggle.setVisible(true);
                    signupToggle.setSelected(false);
                    username.setVisible(true);
                    username.setText("");
                    password.setVisible(true);
                    password.setText("");
                    lsButton.setText("Login");
                    lsTextAlert.setText("");
                    loginText.setText("");
                    lsTitle.setVisible(true);
                    usernameString = "";

                    titleTextField.setDisable(true);
                    authorTextField.setDisable(true);
                    isbnTextField.setDisable(true);
                    addButton.setDisable(true);
                    chooseAFileButton.setDisable(true);
                } else {

                }
            }
        }
    }

    /*
     * Share a new book session
     */
    @FXML private Text chooseFileText;

    @FXML private Text alertText;

    @FXML private TextField titleTextField;

    @FXML private TextField authorTextField;

    @FXML private TextField isbnTextField;

    @FXML private Button chooseAFileButton;

    @FXML private Button addButton;

    private File selectedFile;

    public void shareABookTabSelected(Event event) {

        alertText.setText("");
        chooseFileText.setText("");
        lsTextAlert.setText("");

        if (usernameString.isEmpty() || usernameString.equals("")) {
            titleTextField.setDisable(true);
            authorTextField.setDisable(true);
            isbnTextField.setDisable(true);
            addButton.setDisable(true);
            chooseAFileButton.setDisable(true);
            loginText.setText("");
        } else {
            titleTextField.setDisable(false);
            authorTextField.setDisable(false);
            isbnTextField.setDisable(false);
            addButton.setDisable(false);
            chooseAFileButton.setDisable(false);

            loginToggle.setVisible(false);
            signupToggle.setVisible(false);
            username.setVisible(false);
            password.setVisible(false);
            lsButton.setText("Logout");
            loginText.setText("You're logged in as " + usernameString);
            lsTextAlert.setText("");
        }
    }

    public void chooseFileButtonSelected(ActionEvent event) {
        FileChooser fc = new FileChooser();
        if (!usernameString.isEmpty() && !usernameString.equals("")) {
            selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                chooseFileText.setText("File selected: " + selectedFile);
            } else {
                alertText.setText("No file selected");
            }
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
//                System.out.println("Creating ServerSocket");
                // Create a socket
                Book newBook = new Book(0, usernameString, myAddress.getAddress().getHostAddress(), myAddress.getPort(), titleTextField.getText(), authorTextField.getText(), isbnTextField.getText(), selectedFile.toString(), true);

                // Add new book information to the server
                int status = WebServer.addNewBook(usernameString, newBook.getUser_ip(), newBook.getPort(),
                        newBook.getTitle(), newBook.getIsbn(), newBook.getAuthor(), newBook.getLocation());
                if (status == 201) {
                    System.out.println("New book successfully added to server");
                    alertText.setText("New Book '" + titleTextField.getText() + "' successfully shared");
                } else if (status == 500) {
                    System.out.println("Book already added");
                    alertText.setText("Book already shared");
                } else {
                    System.out.println("Can't share book!");
                    alertText.setText("Can't share book! Please try again!");
                }

                // reset textfields
                titleTextField.clear();
                authorTextField.clear();
                isbnTextField.clear();
                chooseFileText.setText("");
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

    class SharedBookCell extends ListCell<Book> {
        HBox hbox = new HBox();
        Label titleLabel = new Label("");
        Label authorLabel = new Label("");
        Label isbnLabel = new Label("");
        Label locationLabel = new Label("");
        Label portLabel = new Label("");
        Pane pane = new Pane();
        BorderPane titleAuthorBorderPane = new BorderPane();
        BorderPane borderPane = new BorderPane();
        BorderPane statusBorderPane = new BorderPane();
        Button updateLocButton = new Button("Update File's Location");
        CheckBox shareStatus = new CheckBox();

        Book currentBook;

        public SharedBookCell() {
            super();
            titleAuthorBorderPane.setTop(titleLabel);
            titleAuthorBorderPane.setLeft(authorLabel);
            titleAuthorBorderPane.setBottom(isbnLabel);

            borderPane.setTop(portLabel);
            borderPane.setLeft(locationLabel);
            borderPane.setBottom(statusBorderPane);

            statusBorderPane.setTop(shareStatus);
            statusBorderPane.setLeft(updateLocButton);

            hbox.getChildren().addAll(titleAuthorBorderPane, pane, borderPane);
            HBox.setHgrow(pane, Priority.ALWAYS);

            // Update Location Button is pressed
            updateLocButton.setOnAction(e -> {
                FileChooser fc = new FileChooser();
                selectedFile = fc.showOpenDialog(null);

                if (selectedFile != null) {
                    currentBook.setLocation(selectedFile.toString());

                    //update new location with the server
                    boolean status = WebServer.updateBookLocation(currentBook);
                    if (status == true) {
//                        try {
//                            ServerSocket serverSocket = new ServerSocket(currentBook.getPort());
//                            Thread t = new Thread(new ServerRunnable(serverSocket, selectedFile.toString()));
//                            t.start();
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
                        refreshMySharedBooksTab();
                    }
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
                titleLabel.setText("Book title: " + currentBook.getTitle());
                authorLabel.setText("Author: " + currentBook.getAuthor());
                isbnLabel.setText("ISBN: " + currentBook.getIsbn());
                locationLabel.setText("Location:" + currentBook.getLocation());
                portLabel.setText("Port No. : " + currentBook.getPort());
                setGraphic(hbox);

                // check if file exists
                File file = new File(currentBook.getLocation());
                if (!file.exists()) { // if file doesn't exist, show option to update location
                    updateLocButton.setVisible(true);
                    shareStatus.setSelected(false);
                    shareStatus.setText("File doesn't exist! Please Update file!");
                    shareStatus.setTextFill(Color.web("red"));
                } else {
                    updateLocButton.setVisible(false);
                    shareStatus.setSelected(true);
                    shareStatus.setText("Successfully shared!");
                    shareStatus.setTextFill(Color.web("blue"));
                }
            }
        }
    }

    private void refreshMySharedBooksTab() {
        try {
            System.out.println("Refreshing My Shared Books tab");
            ObservableList<Book> list = FXCollections.observableArrayList();

            // load books from the server
            List<Book> bookList = Book.jsonToBookList(WebServer.findAllMySharedBooks(usernameString));
            System.out.println(bookList);

            // Add books to my shared books tab
            for (Book book: bookList) {
                list.add(book);
            }
            mySharedBooksListView.setItems(list);
            mySharedBooksListView.setCellFactory(param -> new SharedBookCell());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mySharedBooksTabSelected(Event event) {
        refreshMySharedBooksTab();
    }

} // end class
