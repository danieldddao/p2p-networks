package chord.Runnable;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import chord.Book;

import java.io.*;
import java.net.Socket;

public class DownloadClient implements Runnable{

    protected Socket socket = null;
    protected Book currentBook = null;
    protected File destinationDir = null;
    protected ProgressBar progressBar = null;
    protected CheckBox checkBox = null;

    public DownloadClient(Socket socket, Book book, File dir, ProgressBar bar, CheckBox checkBox) {
        this.socket = socket;
        this.currentBook = book;
        this.destinationDir = dir;
        this.progressBar = bar;
        this.checkBox = checkBox;
    }

    public void run() {
        try {
            System.out.println("DownloadClient running");

            // Send Book's file location to the owner
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(currentBook.getLocation());
            out.flush();

            // Setup file path
            String filePath = "";
            if (destinationDir.toString().lastIndexOf("/") > 0) {
                filePath = destinationDir + "/";
            } else {
                filePath = destinationDir + "\\";
            }
            filePath += currentBook.getTitle() + currentBook.getLocation().substring(currentBook.getLocation().lastIndexOf("."));
            System.out.println("downloading file's destination: " + filePath);

            // receive file size
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            String fileSizeMsg = (String) in.readObject();
            int fileSize = Integer.parseInt(fileSizeMsg);
            System.out.println("Book's file size " + fileSize);
            byte [] byteArray  = new byte [ fileSize + 1];

            //reading file from socket
            System.out.println("Downloading file");
            InputStream inputStream = socket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            int bytesRead = 0;
            int current = 0;
            bytesRead = inputStream.read(byteArray,0,byteArray.length);					//copying file from socket to byteArray
            current = bytesRead;
            do {
                bytesRead =inputStream.read(byteArray, current, (byteArray.length-current));
                if(bytesRead >= 0) current += bytesRead;
                progressBar.setProgress((current/ (float)fileSize));
//                progressIndicator.setProgress((current/ (float)fileSize));
//                System.out.println("progress: " + (current/ (float)fileSize));
            } while(bytesRead > -1);
            bufferedOutputStream.write(byteArray, 0 , current);							//writing byteArray to file
            bufferedOutputStream.flush();												//flushing buffers

            checkBox.setVisible(true);
            System.out.println("File " + filePath + " downloaded ( size: " + current + " bytes read)");

            out.close();
            in.close();
            inputStream.close();
            fileOutputStream.close();
            bufferedOutputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}