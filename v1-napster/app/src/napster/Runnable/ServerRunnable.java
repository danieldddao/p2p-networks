package napster.Runnable;

import java.io.*;
import java.net.Socket;

public class ServerRunnable implements Runnable{

    protected Socket socket = null;

    public ServerRunnable(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            System.out.println("ServerRunnable running");

            System.out.println("Accepted connection : " + socket);

            // Receive book's file location from the client
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            String message = (String) in.readObject();
            System.out.println("message received " + message);
            if (!message.equals("checking if port available")) {
                String bookLocation = message;
                System.out.println("Location received from client is " + bookLocation);

                if (bookLocation == null) {
                    bookLocation = "";
                }
                // Creating object to send file
                File file = new File(bookLocation);
                byte[] array = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(array, 0, array.length); // copied file into byteArray

                // Sending file size through socket
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("" + array.length);
                out.flush();

                // Sending file through socket
                OutputStream outputStream = socket.getOutputStream();
                System.out.println("Sending " + bookLocation + "( size: " + array.length + " bytes)");
                outputStream.write(array, 0, array.length);            //copying byteArray to socket
                outputStream.flush();                                        //flushing socket
                System.out.println("Done.");                                //file has been sent

                fileInputStream.close();
                bufferedInputStream.close();
                out.close();
                outputStream.close();
            }

            in.close();
            socket.close();

        } catch (StreamCorruptedException SCE) {
            // connection from the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}