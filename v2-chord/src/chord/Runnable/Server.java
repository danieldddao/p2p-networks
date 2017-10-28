package chord.Runnable;

import chord.Components.FingerTable;
import chord.Components.Node;
import chord.Components.Utils;

import java.io.*;

import java.net.Socket;

/**
 * Thread that receives the message from the node and sends response message appropriately
 */
public class Server implements Runnable{

    private Node myNode = null;
    private Socket socket = null;

    public Server(Node node, Socket socket) {
        this.myNode = node;
        this.socket = socket;
    }

    public void run() {
        try {
            // Receive book's file location from the socket
//            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//            String message = (String) in.readObject();

            // Receive message from the client
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object[] messageArray = (Object[]) objectInputStream.readObject();
            System.out.println("Message received: "+ messageArray[0]);
            if (messageArray.length >= 2) {
                System.out.println("Object received: " + messageArray[1]);
            }

            if (messageArray[0] != null) {
                Object response = processMessageRequest(messageArray);
                System.out.println("Response message: " + response + "\n");

                // Send response to the client
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();

//                System.out.println("Server running");
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

//                printStream.close();

                // Wait 50 millisecs to before closing streams
                Thread.sleep(50);
                if (objectOutputStream != null) {objectOutputStream.close();}
            }

            if (objectInputStream != null) {objectInputStream.close();}
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object processMessageRequest(Object[] messageArray) {
        Object response = null;
        try {
            System.out.println("Processing message array: " + messageArray);
            String message = (String) messageArray[0];
            if (message.equals("CHECKING IF PORT IS AVAILABLE")) {
                return "OK";
            }
            long id;

            switch (message) {
                // Check if ID belongs to a node in the network
                case "DOES ID EXIST":
                    id = (long) messageArray[1];
                    response = "NOT EXIST";
                    System.out.println("Checking if ID exists: " + id);

                    // Check my nodeID
                    if (id == myNode.getNodeId()) {
                        System.out.println("ID belongs to me");
                        response = "ALREADY EXIST";
                        break; // get out of switch statement
                    }

                    // Check my successor and predecessor
                    if ((myNode.getSuccessor() != null && id == myNode.getSuccessor().getNodeId()) || (myNode.getPredecessor() != null && id == myNode.getPredecessor().getNodeId())) {
                        System.out.println("ID belongs to my predecessor or successor");
                        response = "ALREADY EXIST";
                        break; // get out of switch statement
                    }

                    // Check the finger table
                    System.out.println("Checking finger table");
                    FingerTable fingerTable = myNode.getFingerTable();
                    int iThFinger = fingerTable.findIthFingerOf(id); // Find the finger that stores information of the node ID
                    if (iThFinger == 0) {
                        System.out.println("ID " + id + " is too large");
                        response = "ALREADY EXIST";
                        break; // get out of switch statement
                    }

                    System.out.println("Found ID in the " + iThFinger + "-th finger");
                    fingerTable.printFingerTable();
                    Node entryNode = fingerTable.getEntryNode(iThFinger);
                    if (entryNode != null) {   // If ID is already assigned to a node
                        // Contact that node to see if ID belongs to another node
                        System.out.println("Contacting node #" + entryNode.getNodeId() + " (" + entryNode.getAddress().getAddress().getHostAddress() + ":" + entryNode.getAddress().getPort() + ")...");
                        Object[] objArray = new Object[2];
                        objArray[0] = "DOES ID EXIST";
                        objArray[1] = id;
                        response = Utils.sendMessage(entryNode.getAddress(), objArray);
                    }

                    break;


                // New node is joining the network, find its successor
                // Response is the successor node
                case "JOINING. FIND MY SUCCESSOR":
                    id = (long) messageArray[1];
                    System.out.println("New node wants to find its successor: " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
                    Node successor = myNode.findSuccessorOf(id);
                    System.out.println("Found new node's successor: id=" + successor.getNodeId() + ", address:" + successor.getAddress().getAddress().getHostAddress() + ":" + successor.getAddress().getPort());
                    response = successor;

                    break;


                // Response is a Node
                case "GET YOUR SUCCESSOR":
                    response = myNode.getSuccessor();
                    break;


                // My predecessor has changed, updating my predecessor
                case "I AM YOUR NEW PREDECESSOR":
                    Node pre = (Node) messageArray[1];
                    myNode.setPredecessor(pre);
                    response = "GOT IT";
                    break;

            }
            return response;
        } catch (Exception e){
            e.printStackTrace();
            return response;
        }
    }
}