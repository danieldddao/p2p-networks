package chord.Components;

import javafx.util.Pair;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class FingerTable {

    private HashMap<Integer, Pair<InetSocketAddress, Long>> entryNodes = null;
    private HashMap<Integer, Pair<Long, Long>> ranges = null;
    private int transitionFinger = 0; // finger that transitions from the max ID to 0 in a circle
    private Long nodeID = null;

    public FingerTable(long nodeID) {
        this.nodeID = nodeID;
        this.entryNodes = new HashMap();
        this.ranges = new HashMap();
        for (int i = 1; i <= Node.getM(); i++) {
            updateEntryNode(i, null, null);
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
     * Update ith finger's entry with host address and nodeID in the finger table
     * @param i
     * @param address
     * @param id
     */
    public void updateEntryNode(int i, InetSocketAddress address, Long id) {
        try {
            entryNodes.put(i, new Pair(address, id));

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

    public Pair<InetSocketAddress, Long> getEntryNode(int i) {
        try {
            return entryNodes.get(i);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
        if (ID == nodeID || ID >= Node.getChordRingSize()) {
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
            if (entryNodes.get(i).getKey() != null) {
                address = entryNodes.get(i).getKey().getHostString();
            }
            if (entryNodes.get(i).getValue() != null) {
                nodeID = "" + entryNodes.get(i).getValue();
            }
//            System.out.println(i + "\t " +
//                                ranges.get(i).getKey() + ".." + ranges.get(i).getValue() + "\t\t\t\t" +
//                                address + ", " + nodeID);
            System.out.println(String.format( "%1s \t\t %-8s \t\t  %-8s", i, ranges.get(i).getKey() + ".." + ranges.get(i).getValue(), address + ", " + nodeID));
        }
        System.out.println();
    }
}
