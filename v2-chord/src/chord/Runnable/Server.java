package chord.Runnable;

import chord.Components.FingerTable;
import chord.Components.MessageType;
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
            System.out.println(myNode.getNodeName() + "-SERVER: Message received: "+ messageArray[0]);
            if (messageArray.length >= 2) {
                System.out.println("SERVER: Object received: " + messageArray[1]);
            }

            if (messageArray[0] != null) {
                Object response = processMessageRequest(messageArray);
                System.out.println(myNode.getNodeName() + "-SERVER: Response message: " + response + "\n");

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
            System.out.println(myNode.getNodeName() + "-SERVER: Processing message array: " + messageArray);
            MessageType message = (MessageType) messageArray[0];
            if (message == MessageType.CHECKING_IF_PORT_IS_AVAILABLE) {
                return MessageType.OK;
            }
            long id;

            switch (message) {
                // Check if ID belongs to a node in the network
                case DOES_ID_EXIST:
                    id = (long) messageArray[1];
                    response = MessageType.NOT_EXIST;
                    System.out.println(myNode.getNodeName() + "-SERVER: Checking if ID exists: " + id);

                    // Check my nodeID
                    if (id == myNode.getNodeId()) {
                        System.out.println(myNode.getNodeName() + "-SERVER: ID belongs to me");
                        response = MessageType.ALREADY_EXIST;
                        break; // get out of switch statement
                    }

                    // Check my successor and predecessor
                    if ((myNode.getSuccessor() != null && id == myNode.getSuccessor().getNodeId()) || (myNode.getPredecessor() != null && id == myNode.getPredecessor().getNodeId())) {
                        System.out.println(myNode.getNodeName() + "-SERVER: ID belongs to my predecessor or successor");
                        response = MessageType.ALREADY_EXIST;
                        break; // get out of switch statement
                    }

                    // Check the finger table
                    System.out.println(myNode.getNodeName() + "-SERVER: Checking finger table");
                    FingerTable fingerTable = myNode.getFingerTable();
                    int iThFinger = fingerTable.findIthFingerOf(id); // Find the finger that stores information of the node ID
                    if (iThFinger == 0) {
                        System.out.println(myNode.getNodeName() + "-SERVER: ID " + id + " is too large");
                        response = MessageType.ALREADY_EXIST;
                        break; // get out of switch statement
                    }

                    System.out.println(myNode.getNodeName() + "-SERVER: Found ID in the finger # " + iThFinger);
                    fingerTable.printFingerTable();
                    // If ID is in the 1st finger and if ID exists, ID must be my successor.
                    if (iThFinger == 1 && myNode.getSuccessor().getNodeId() != id) {
                        System.out.println(myNode.getNodeName() + "-SERVER: ID is not my successor, so it doesn't exist");
                        break;
                    }
                    Node entryNode = fingerTable.getEntryNode(iThFinger);
                    if (entryNode != null && entryNode.getNodeId() != myNode.getNodeId()) {   // If ID is already assigned to a node which is not me
                        // Contact that node to see if ID belongs to another node
                        System.out.println(myNode.getNodeName() + "-SERVER: Contacting node #" + entryNode.getNodeId() + " (" + entryNode.getAddress().getAddress().getHostAddress() + ":" + entryNode.getAddress().getPort() + ")...");
                        Object[] objArray = new Object[2];
                        objArray[0] = MessageType.DOES_ID_EXIST;
                        objArray[1] = id;
                        response = Utils.sendMessage(entryNode.getAddress(), objArray);
                    }
                    break;


                // New node is joining the network, find its successor
                // Response is the successor node
                case FIND_SUCCESSOR:
                    id = (long) messageArray[1];
                    System.out.println(myNode.getNodeName() + "-SERVER: A node wants to find successor of id=" + id);
                    Node successor = myNode.findSuccessorOf(id);
                    System.out.println(myNode.getNodeName() + "-SERVER: Found new node's successor: " + successor.getNodeName() + ", address:" + successor.getAddress().getAddress().getHostAddress() + ":" + successor.getAddress().getPort());
                    response = successor;
                    break;


                // Find closest finger preceding id
                // Response is a node
                case CLOSEST_PRECEDING_FINGER:
                    id = (long) messageArray[1];
                    System.out.println(myNode.getNodeName() + "-SERVER: A node wants to find closest finger preceding id=" + id);
                    Node closestFinger = myNode.closestPrecedingFingerOf(id);
                    System.out.println(myNode.getNodeName() + "-SERVER: Found closest finger preceding id: " + closestFinger.getNodeName() + ", address:" + closestFinger.getAddress().getAddress().getHostAddress() + ":" + closestFinger.getAddress().getPort());
                    response = closestFinger;
                    break;


//                // Response is a my successor Node
                case GET_YOUR_SUCCESSOR:
                    System.out.println(myNode.getNodeName() + "-SERVER: A node wants to get my successor " + myNode.getNodeName());
                    response = myNode.getSuccessor();
                    System.out.println(myNode.getNodeName() + "-SERVER: Return my successor: " + myNode.getSuccessor().getNodeName() + ", address:" + myNode.getSuccessor().getAddress().getAddress().getHostAddress() + ":" + myNode.getSuccessor().getAddress().getPort());
                    break;


                // Return my predecessor
                // Response is my predecessor a Node
                case GET_YOUR_PREDECESSOR:
                    System.out.println(myNode.getNodeName() + "-SERVER: A node wants to find predecessor of me " + myNode.getNodeName());
                    response = myNode.getPredecessor();
                    System.out.println(myNode.getNodeName() + "-SERVER: Return my predecessor: " + myNode.getPredecessor().getNodeName() + ", address:" + myNode.getPredecessor().getAddress().getAddress().getHostAddress() + ":" + myNode.getPredecessor().getAddress().getPort());
                    break;


                // My predecessor has changed, updating my predecessor
                case I_AM_YOUR_NEW_PREDECESSOR:
                    Node pre = (Node) messageArray[1];
                    myNode.setPredecessor(pre);
                    response = MessageType.GOT_IT;
                    System.out.println(myNode.getNodeName() + "-SERVER: new predecessor id=" + pre.getNodeName() + ", address:" + pre.getAddress().getAddress().getHostAddress() + ":" + pre.getAddress().getPort());

                    // Check if I need to update my successor
//                    if (myNode.getNodeId() == myNode.getSuccessor().getNodeId()) { // Only I was in the network, now I have a new successor
//                        myNode.setSuccessor(pre);
//                    }
                    break;


                // My successor has changed, updating my successor
                case I_AM_YOUR_NEW_SUCCESSOR:
                    Node suc = (Node) messageArray[1];
                    myNode.setSuccessor(suc);
                    // Update my 1-st finger as well
                    myNode.getFingerTable().updateEntryNode(1, suc);
                    response = MessageType.GOT_IT;
                    System.out.println(myNode.getNodeName() + "-SERVER: new predecessor id=" + suc.getNodeName() + ", address:" + suc.getAddress().getAddress().getHostAddress() + ":" + suc.getAddress().getPort());
                    break;


                // Update i-th finger with node p
                case UPDATE_FINGER_TABLE:
                    Object[] msgObjArray = (Object[]) messageArray[1];
                    int i = (int) msgObjArray[0];
                    Node n = (Node) msgObjArray[1];
                    System.out.println(myNode.getNodeName() + "-SERVER: Updating " + i + "-th finger with new node entry: " + n.getNodeName() + ", " + n.getAddress().getAddress().getHostAddress() + ":" + n.getAddress().getPort());
                    myNode.updateFingerTable(i, n);
                    myNode.getFingerTable().printFingerTable();
                    response = MessageType.GOT_IT;
                    break;

                case PRINT_YOUR_FINGER_TABLE:
                    myNode.getFingerTable().printFingerTable();
                    response = MessageType.OK;
                    break;
            }
            return response;
        } catch (Exception e){
            e.printStackTrace();
            return response;
        }
    }
}