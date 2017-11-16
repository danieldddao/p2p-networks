package chord.Runnable;

import chord.Components.MessageType;
import chord.Components.Node;
import chord.Components.Utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This thread will periodically verify my immediate successor
 * and tell the successor about me.
 */
public class Stabilize implements Runnable {

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

                /*
                 * Check if my successor is still alive
                 */
                Socket socket = new Socket();
                Node mySuc = myNode.getSuccessor();
                System.out.println("Connecting to " + mySuc.getAddress().getAddress().getHostAddress() + ":" + myNode.getSuccessor().getAddress().getPort());
                socket.connect(mySuc.getAddress(), 1000);

//                // Send message to server to check port availability
//                objArray[0] = MessageType.CHECKING_IF_PORT_IS_AVAILABLE;
//                ObjectOutputStream objectOutputStream = new ObjectOutputStream(ignored.getOutputStream());
//                objectOutputStream.writeObject(objArray);
//                objectOutputStream.flush();
//
//                // Receive response message
//                ObjectInputStream objectInputStream = new ObjectInputStream(ignored.getInputStream());
//                objectInputStream.readObject();
//
//                objectInputStream.close();
//                objectOutputStream.close();
//                ignored.close();


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
