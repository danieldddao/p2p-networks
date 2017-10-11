package napster;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {

    /*
     * Search a book tab
     */

    @FXML
    private TextField searchTextField;

    public void searchBook(ActionEvent event) {
        try {
            WebServer.searchBook(searchTextField.getText());
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
