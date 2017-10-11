package napster;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketRunnable implements Runnable{

    protected ServerSocket serverSocket = null;
    protected String bookLocation   = null;

    public SocketRunnable(ServerSocket socket, String bookLocation) {
        this.serverSocket = socket;
        this.bookLocation = bookLocation;
    }

    public void run() {
        try {
            System.out.println("Waiting for receiver...\n\n");
            Socket socket = serverSocket.accept();
            System.out.println("Accepted connection : " + socket);
            System.out.println("Thread running for port " + socket.getLocalPort());
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
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}