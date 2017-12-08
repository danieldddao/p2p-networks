package decentralizedP2P.RunnableThread;

import decentralizedP2P.Components.Book;
import decentralizedP2P.Components.MessageType;
import decentralizedP2P.Components.Node;
import decentralizedP2P.Components.Utils;
import javafx.util.Pair;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
                    if (mySuc.getNodeId() != this.myNode.getNodeId()) {
//                        System.out.println("Connecting to " + mySuc.getAddress().getAddress().getHostAddress() + ":" + myNode.getSuccessor().getAddress().getPort());

                        /*
                         * Check if my successor is still alive
                         */
                        // Send message to my successor to check if it's still alive
                        socket.connect(mySuc.getAddress(), 1000);
                        objArray[0] = MessageType.ARE_YOU_STILL_ALIVE;
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.writeObject(objArray);
                        objectOutputStream.flush();

                        // Wait 50 millisecs to receive the response
                        Thread.sleep(50);

                        // Receive response suc's book list, which contains books assigned to my suc
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        List<Book> response = (List<Book>) objectInputStream.readObject();
                        myNode.setMySucBookList(response);

                        objectInputStream.close();
                        objectOutputStream.close();
                        socket.close();

                        /*
                         * Get my successor's shared books
                         */
                        objArray[0] = MessageType.GIVE_YOUR_SHARED_BOOKS;
                        List<Pair<Long, String>> sharedBooks = (List<Pair<Long, String>>) Utils.sendMessage(mySuc.getAddress(), objArray);
                        myNode.setMySucSharedBooks(sharedBooks);

                        /*
                         * Get my successor's successor
                         */
                        objArray[0] = MessageType.GET_YOUR_SUCCESSOR;
                        Node sucSuc = (Node) Utils.sendMessage(mySuc.getAddress(), objArray);
                        if (sucSuc != null) {
//                            System.out.println(myNode.getNodeName() + " - STABILIZE - Found successor of my successor: " + sucSuc.getNodeName());
                            this.myNode.setSucSuccessor(sucSuc);
                        }

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
                            System.out.println(myNode.getNodeName() + " - STABILIZE - My successor has changed! Notify my new successor...");
                            myNode.notifyMyNewSuccessor(pre.getAddress());
                        }
                    } else {
                        this.myNode.setSucSuccessor(this.myNode);
                    }
                } catch (SocketTimeoutException | ConnectException | NullPointerException e) {
                    /*
                     * My successor is no longer available
                     */
                    System.out.println(myNode.getNodeName() + " - STABILIZE - My successor is no longer available: " + mySuc.getNodeName());

                    // Remove all my successor's shared books
                    System.out.println(myNode.getNodeName() + " - STABILIZE - Removing my successor's share books: " + myNode.getMySucSharedBooks().size());
                    for (Pair<Long, String> book : myNode.getMySucSharedBooks()) {
                        String titleAddress = book.getValue() + myNode.getSuccessor().getAddress().getAddress().getHostAddress() + ":" + myNode.getSuccessor().getAddress().getPort();
                        System.out.println(myNode.getNodeName() + " - STABILIZE - Remove shared book: " + book.getKey() + ", " + titleAddress);
                        myNode.removeSharedBook(new Pair<Long, String>(book.getKey(), titleAddress));
                    }

                    // Find my new successor
                    System.out.println(myNode.getNodeName() + " - STABILIZE - My successor left the network! Find my new successor");
                    Node newSuc = myNode.getSucSuccessor();
                    System.out.println(myNode.getNodeName() + " - STABILIZE - My successor left the network! Found my new successor: " + newSuc.getNodeName());
                    myNode.setSuccessor(newSuc);
                    myNode.getFingerTable().updateEntryNode(1, newSuc);

                    // Remove books shared by my old successor and my old successor's holding them
                    List<Book> myOldSucBookList = new ArrayList();
                    for (Book b : myNode.getMySucBookList()) {
                        // If this book is not shared by my old successor, move it to my new successor
                        if (!b.getOwnerAddress().getAddress().getHostAddress().equals(mySuc.getAddress().getAddress().getHostAddress()) && b.getOwnerAddress().getPort() != mySuc.getAddress().getPort()) {
                            myOldSucBookList.add(b);
                        }
                    }

                    // And transfer books from my old successor to my new successor
                    System.out.println(myNode.getNodeName() + " - STABILIZE - My successor left the network! Transferring old successor's books to new successor");
                    objArray = new Object[2];
                    objArray[0] = MessageType.YOU_HAVE_NEW_BOOKS;
                    objArray[1] = myOldSucBookList;
                    MessageType response = (MessageType) Utils.sendMessage(myNode.getSuccessor().getAddress(), objArray);
                    if (response == MessageType.GOT_IT) {
                        System.out.println(myNode.getNodeName() + " - STABILIZE - Successfully transferred books to new successor");
                    } else {
                        System.out.println(myNode.getNodeName() + " - STABILIZE - Failed to transfer books to new successor");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
