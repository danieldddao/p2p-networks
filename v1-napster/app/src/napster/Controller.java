package napster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;

import napster.Book;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.*;

public class Controller {

    /*
     * Search a book tab
     */

    @FXML
    private TextField searchTextField;

    @FXML
    private Text searchAlertText;

    @FXML
    private ListView<Book> bookListView;

    static class BookCell extends ListCell<Book> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Button button = new Button("Download");
        Book currentBook;

        public BookCell() {
            super();
            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            button.setOnAction(e -> {
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
                setGraphic(hbox);
            }
        }
    }

    public void searchBook(ActionEvent event) {
        try {
            searchAlertText.setText("");
//            String searchBookResult = WebServer.searchBook(searchTextField.getText());
            String searchBookResult = "[{\"id\":1,\"user_ip\":\"0.0.0.0/0.0.0.0\",\"port_number\":\"1111\",\"title\":\"EV\",\"isbn\":\"\",\"author\":\"dd\",\"location\":\"/Users/danieldao/Downloads/Evaluation form-3.docx\"},{\"id\":2,\"user_ip\":\"0.0.0.0/0.0.0.0\",\"port_number\":\"1112\",\"title\":\"jb\",\"isbn\":\"\",\"author\":\"dd\",\"location\":\"/Users/danieldao/Downloads/jb.ttc\"}]\n";
            if (searchBookResult.isEmpty()) {
                searchAlertText.setText("No Book found!");
            } else {
                ObservableList<Book> list = FXCollections.observableArrayList();
                JSONArray jsonarray = new JSONArray(searchBookResult);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    int id = jsonobject.getInt("id");
                    String user_ip = jsonobject.getString("user_ip");
                    String port = jsonobject.getString("port_number");
                    String title = jsonobject.getString("title");
                    String isbn = jsonobject.getString("isbn");
                    String author = jsonobject.getString("author");
                    String location = jsonobject.getString("location");
                    Book newBook = new Book(id, user_ip, port, title, author, isbn, location);
                    list.add(newBook);
                }
                bookListView.setItems(list);
                bookListView.setCellFactory(param -> new BookCell());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadBook(ActionEvent event) {
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

        try {
            System.out.println("download button pressed");
            int bytesRead=0;
            int current = 0;
//            FileOutputStream fileOutputStream = null;
//            BufferedOutputStream bufferedOutputStream = null;
//            Socket socket = null;
            try {
                //creating connection.
                Socket socket = new Socket("0.0.0.0", 1111);
                System.out.println("connected.");

                // receive file size
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String fileSizeMsg = (String) in.readObject();
                int fileSize = Integer.parseInt(fileSizeMsg);
                System.out.println("file size " + fileSize);
                byte [] byteArray  = new byte [ fileSize + 1];					//I have hard coded size of byteArray, you can send file size from socket before creating this.
                System.out.println("Downloading file");

                //reading file from socket
                InputStream inputStream = socket.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream("/Users/danieldao/Downloads/jb1.ttc");
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                bytesRead = inputStream.read(byteArray,0,byteArray.length);					//copying file from socket to byteArray

                current = bytesRead;
                do {
                    bytesRead =inputStream.read(byteArray, current, (byteArray.length-current));
                    if(bytesRead >= 0) current += bytesRead;
                    System.out.println((current/ (float)fileSize) * 100);
                } while(bytesRead > -1);
                bufferedOutputStream.write(byteArray, 0 , current);							//writing byteArray to file
                bufferedOutputStream.flush();												//flushing buffers

                System.out.println("File " + "/Users/danieldao/Downloads/road1.png"  + " downloaded ( size: " + current + " bytes read)");
                in.close();
                fileOutputStream.close();
                bufferedOutputStream.close();
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*
     * Share a book tab
     */
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

    public void chooseFileButtonSelected(ActionEvent event) {
        FileChooser fc = new FileChooser();
        selectedFile = fc.showOpenDialog(null);

        if (selectedFile != null) {
            chooseFileText.setText("File selected: " + selectedFile);
//            System.out.println("selected file: " + selectedFile.toString());
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
                System.out.println("Creating ServerSocket");
                // Create a socket
                ServerSocket serverSocket = new ServerSocket(initialPort);

                // Add book information to server
                int status = WebServer.addNewBook(serverSocket.getInetAddress().toString(), "" + serverSocket.getLocalPort(),
                                    titleTextField.getText(), isbnTextField.getText(), authorTextField.getText(), selectedFile.toString());
                if (status == 201) {
                    System.out.println("New book successfully added to server");
                    alertText.setText("New Book '" + titleTextField.getText() + "' successfully shared");
                    initialPort ++;
                    System.out.println("ServerSocket created: " + serverSocket.getLocalSocketAddress());
                    Thread t = new Thread(new SocketRunnable(serverSocket, selectedFile.toString()));
                    t.start();
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
        } catch (BindException be) {
            alertText.setText("Something is wrong! Please try again!");
            initialPort++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//        try {
//            int bytesRead;
//            int current = 0;
//
//            ServerSocket serverSocket = null;
//            serverSocket = new ServerSocket(1234);
//
//            Socket clientSocket = null;
//            clientSocket = serverSocket.accept();
//            System.out.println("accepted");
//            InputStream in = clientSocket.getInputStream();
//
//            // Writing the file to disk
//            // Instantiating a new output stream object
//            OutputStream output = new FileOutputStream(testFile);
//
//            byte[] buffer = new byte[1024];
//            while ((bytesRead = in.read(buffer)) != -1) {
//                output.write(buffer, 0, bytesRead);
//                System.out.println("writing " + bytesRead);
//            }
//            // Closing the FileOutputStream handle
//            output.close();
//            serverSocket.close();
//            clientSocket.close();
//            System.out.println("Done");
//        } catch (Exception e) {
//
//        }


//        FileInputStream fileInputStream = null;
//        BufferedInputStream bufferedInputStream = null;

//        OutputStream outputStream = null;
//        ServerSocket serverSocket = null;
//        Socket socket = null;

} // end class
