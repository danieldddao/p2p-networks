package decentralizedP2P.RunnableThread;

import decentralizedP2P.Components.Node;

import java.io.Serializable;
import java.util.Random;

/**
 * This thread will periodically refresh finger table entries.
 */
public class FixFingers implements Runnable, Serializable {
    private final int periodTime = 100;
    private Node myNode = null;
    private Random random = null;
    private static boolean isRunning = true;

    public FixFingers(Node node) {
        try {
            this.myNode = node;
            this.random = new Random();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("FixFingers running");
            while (isRunning) {
                int i = random.nextInt(Node.getM()-1) + 2; // Get a random i-th finger

                // Find successor of i-th start position
                Node entryNode = myNode.getFingerTable().getEntryNode(i);
                Node n = myNode.findSuccessorOf(myNode.getFingerTable().getRange(i).getKey());
                if (entryNode.getNodeId() != n.getNodeId()) {
                    System.out.println(myNode.getNodeName() + " - FIXFINGERS - updating finger entry # " + i + " , new entry: " + n.getNodeName());
                    myNode.getFingerTable().updateEntryNode(i, n);

                    // If my successor has changed
                    if (i == 1 && n.getNodeId() != myNode.getSuccessor().getNodeId()) {
                        System.out.println(myNode.getNodeName() + " - FIXFINGERS - my successor has changed from " + myNode.getSuccessor().getNodeName() + " to " + n.getNodeName());
                        myNode.setSuccessor(n);
                    }
                }

                Thread.sleep(periodTime);
            }
            System.out.println("FixFingers closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeFixFingers() {
        isRunning = false;
    }
}
