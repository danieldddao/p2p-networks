package chord.Runnable;

import chord.Components.Node;

import java.io.*;
import java.net.Socket;

/**
 * Thread that receives the message from the node and sends response message appropriately
 */
public class ServerRunnable implements Runnable{

    private Node myNode = null;
    private Socket socket = null;

    public ServerRunnable(Node node, Socket socket) {
        this.myNode = node;
        this.socket = socket;
    }

    public void run() {
        try {
            // Receive book's file location from the socket
//            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//            String message = (String) in.readObject();

            // Receive message from the client
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String message = reader.readLine();
            System.out.println("Message received: "+ message);

            // Send response to the client
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println("OK");

            if (!message.equals("checking if port available") && message != null) {
                processMessageRequest(message);
//                System.out.println("ServerRunnable running");
//                System.out.println("Accepted connection : " + socket);
//
//                String bookLocation = message;
//                System.out.println("Location received from socket is "+ bookLocation);
//
//                if (bookLocation == null) {bookLocation = "";}
//                // Creating object to send file
//                File file = new File(bookLocation);
//                byte[] array = new byte[(int) file.length()];
//                FileInputStream fileInputStream = new FileInputStream(file);
//                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//                bufferedInputStream.read(array, 0, array.length); // copied file into byteArray
//
//                // Sending file size through socket
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                out.writeObject("" + array.length);
//                out.flush();
//
//                // Sending file through socket
//                OutputStream outputStream = socket.getOutputStream();
//                System.out.println("Sending " + bookLocation + "( size: " + array.length + " bytes)");
//                outputStream.write(array, 0, array.length);            //copying byteArray to socket
//                outputStream.flush();                                        //flushing socket
//                System.out.println("Done.");                                //file has been sent
//
//                fileInputStream.close();
//                bufferedInputStream.close();
//                out.close();
//                outputStream.close();
            }

            inputStream.close();
            reader.close();
            outputStream.close();
            printStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String processMessageRequest(String message) {
        try {
            System.out.println("Processing message: " + message);
            String[] splitted = message.split("_");
            String responseMessage = null;

            switch (splitted[0]) {
                case "FINDSUCCESSOR":
                    System.out.println("socket wants to find successor of " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
                    break;

            }
            System.out.println("Response message: " + responseMessage + "\n");
            return responseMessage;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}