package chord.Components;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.HashMap;

public class FingerTable implements Serializable {

    private HashMap<Integer, Node> entryNodes = null;
    private HashMap<Integer, Pair<Long, Long>> ranges = null;
    private int transitionFinger = 0; // finger that transitions from the max ID to 0 in a circle
    private Long nodeID = null;

    public FingerTable(Node node) {
        this.nodeID = node.getNodeId();
        this.entryNodes = new HashMap();
        this.ranges = new HashMap();
        for (int i = 1; i <= Node.getM(); i++) {
            updateEntryNode(i, node);
            if (i == 1) {
                updateRange(i, nodeID + 1, nodeID + 1);
            } else {
                long lowerVal = (long) (nodeID + Math.pow(2, i-1)) % Node.getChordRingSize();
                long upperVal = (long) (lowerVal + Math.pow(2, i-1) - 1) % Node.getChordRingSize();
                if ((lowerVal + Math.pow(2, i-1) - 1) >= Node.getChordRingSize()) {transitionFinger = i;}
                updateRange(i, lowerVal, upperVal);
            }
        }

        printFingerTable();
    }

    /**
     * Update ith finger's entry with a node in the finger table
     * @param i
     * @param newEntryNode
     */
    public void updateEntryNode(int i, Node newEntryNode) {
        try {
            if (newEntryNode != null) {
                System.out.println("Updating entry " + i + "-th with new node having address: " + newEntryNode.getAddress().getAddress().getHostAddress() + ":" +  newEntryNode.getAddress().getPort() + " and id " + newEntryNode.getNodeId());
            }
            entryNodes.put(i, newEntryNode);
//            // if the updated one is successor, notify the new successor
//            if (i == 1 && value != null && !value.equals(localAddress)) {
//                notify(value);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update ith finger's range with lowerbound and upperbound the finger table
     * @param i
     * @param lowerVal
     * @param upperVal
     */
    public void updateRange(int i, long lowerVal, long upperVal) {
        try {
            ranges.put(i, new Pair(lowerVal, upperVal));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get entry node of the ith finger
     * @param i
     * @return entry node of i-th finger
     */
    public Node getEntryNode(int i) {
        try {
            return entryNodes.get(i);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the range of the i-th finger
     * @param i
     * @return range of the i-th finger
     */
    public Pair<Long, Long> getRange(int i) {
        try {
            return ranges.get(i);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find i-th finger that stores the ID
     * @param ID
     * @return i-th finger
     */
    public int findIthFingerOf(long ID) {
        if (ID >= Node.getChordRingSize()) {
            return 0;
        } else if (ID < nodeID) { // ID is smaller than nodeID, search from transition finger i-th
            Pair<Long, Long> range = getRange(transitionFinger);
            if (ID >= 0 && ID <= range.getValue()) {
                return transitionFinger;
            } else {
                int i = transitionFinger + 1;
                for (; i <= Node.getM(); i++) {
                    range = getRange(i);
                    if (ID >= range.getKey() && ID <= range.getValue()) { // Found node ID in the ith finger
                        break;
                    }
                }
                return i;
            }
        } else { // ID is larger than nodeID, search from lowest i-th
            int i = 1;
            for (; i <= Node.getM(); i++) {
                Pair<Long, Long> range = getRange(i);
                if (ID >= range.getKey() && ID <= range.getValue()) { // Found node ID in the ith finger
                    break;
                } else if (i == transitionFinger && ID >= range.getKey() && ID >= range.getValue()) {
                    break;
                }
            }
            return i;
        }
    }

    public void printFingerTable() {
        System.out.println("\nFinger Table:");
        System.out.println("i\t\t range\t\t\t entry");
        for (int i = 1; i <= Node.getM(); i++) {
            String address = "null";
            String nodeID = "null";
            if (entryNodes.get(i) != null) {
                address = entryNodes.get(i).getAddress().getHostString();
                nodeID = "" + entryNodes.get(i).getNodeId();
            }
            System.out.println(String.format( "%1s \t\t %-8s \t\t  %-8s", i, ranges.get(i).getKey() + ".." + ranges.get(i).getValue(), address + ", " + nodeID));
        }
        System.out.println();
    }
}
