package chord.Runnable;

import chord.Components.Book;
import chord.Components.MessageType;
import chord.Components.Node;
import chord.Components.Utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

/**
 * This thread will periodically verify my immediate successor
 * and tell the successor about me.
 */
public class Stabilize implements Runnable, Serializable {

    private final int periodTime = 100;
    private Node myNode = null;
    private static boolean isRunning = true;

    public Stabilize(Node node) {
        try {
            this.myNode = node;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("Stabilize running");
            Object[] objArray = new Object[1];
            while (isRunning) {

                Socket socket = new Socket();
                Node mySuc = myNode.getSuccessor();
                try {
                    System.out.println("Connecting to " + mySuc.getAddress().getAddress().getHostAddress() + ":" + myNode.getSuccessor().getAddress().getPort());
                    socket.connect(mySuc.getAddress(), 1000);

                    /*
                     * Check if my successor is still alive
                     */
                    // Send message to my successor to check if it's still alive
                    objArray[0] = MessageType.ARE_YOU_STILL_ALIVE;
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(objArray);
                    objectOutputStream.flush();

                    // Wait 50 millisecs to receive the response
                    Thread.sleep(50);

                    // Receive response message
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    List<Book> response = (List<Book>) objectInputStream.readObject();
                    myNode.setMySucBookList(response);

                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();


                    /*
                     * Check if my successor has changed
                     */
                    objArray[0] = MessageType.GET_YOUR_PREDECESSOR;
                    Node pre = (Node) Utils.sendMessage(myNode.getSuccessor().getAddress(), objArray);

                    // If pre is between me and my successor, pre should be my successor
                    if (Utils.isIdBetweenNotEq(pre.getNodeId(), myNode.getNodeId(), myNode.getSuccessor().getNodeId())) {
                        System.out.println(myNode.getNodeName() + " - STABILIZE - found my new successor: " + pre.getNodeName());
                        myNode.setSuccessor(pre);

                        // Update my 1st entry in the finger table
                        myNode.getFingerTable().updateEntryNode(1, pre);
                    }

                    // Notify my new successor that I'm its new predecessor
                    if (pre.getNodeId() != myNode.getNodeId()) {
                        System.out.println(myNode.getNodeName() + " - My successor has changed! Notify my new successor...");
                        myNode.notifyMyNewSuccessor(pre.getAddress());
                    }
                } catch (Exception e) {
                    // My successor is no longer available
                    // Find my new successor
                    List<Book> myOldSucBookList = myNode.getMySucBookList();
                    System.out.println(myNode.getNodeName() + " - My successor left the network! Find my new successor");
                    Node newSuc = myNode.findSuccessorOf(myNode.getNodeId());
                    System.out.println(myNode.getNodeName() + " - My successor left the network! Found my new successor: " + newSuc.getNodeName());
                    myNode.setSuccessor(newSuc);
                    myNode.getFingerTable().updateEntryNode(1, newSuc);

                    // And transfer books from my old successor to my new successor
                    System.out.println(myNode.getNodeName() + " - My successor left the network! Transferring old successor's books to new successor");
                    objArray = new Object[2];
                    objArray[0] = MessageType.YOU_HAVE_NEW_BOOKS;
                    objArray[1] = myOldSucBookList;
                    MessageType response = (MessageType) Utils.sendMessage(myNode.getSuccessor().getAddress(), objArray);
                    if (response == MessageType.GOT_IT) {
                        System.out.println(myNode.getNodeName() + " - Successfully transferred books to new successor");
                    } else {
                        System.out.println(myNode.getNodeName() + " - Failed to transfer books to new successor");
                    }
                }

                Thread.sleep(periodTime);
            }
            System.out.println("Stabilize closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeStabilize() {
        isRunning = false;
    }

}
