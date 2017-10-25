package chord.Components;

import javafx.util.Pair;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class FingerTable {

    private HashMap<Integer, Pair<InetSocketAddress, Long>> entries = null;
    private HashMap<Integer, Pair<Long, Long>> ranges = null;

    public FingerTable(long nodeID) {
        this.entries = new HashMap();
        this.ranges = new HashMap();
        for (int i = 1; i <= Node.getM(); i++) {
            updateEntry(i, null, null);
            if (i == 1) {
                updateRange(i, nodeID + 1, nodeID + 1);
            } else {
                long lowerVal = (long) (nodeID + Math.pow(2, i-1)) % Node.getChordRingSize();
                long upperVal = (long) (lowerVal + Math.pow(2, i-1) - 1) % Node.getChordRingSize() ;
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
    public void updateEntry(int i, InetSocketAddress address, Long id) {
        try {
            entries.put(i, new Pair(address, id));

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

    public Pair<InetSocketAddress, Long> getEntry(int i) {
        try {
            return entries.get(i);
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

    public void printFingerTable() {
        System.out.println("\nFinger Table:");
        System.out.println("i\t range\t\t entry");
        for (int i = 1; i <= Node.getM(); i++) {
            String address = "null";
            String nodeID = "null";
            if (entries.get(i).getKey() != null) {
                address = entries.get(i).getKey().getHostString();
            }
            if (entries.get(i).getValue() != null) {
                nodeID = "" + entries.get(i).getValue();
            }
            System.out.println(i + "\t " +
                                ranges.get(i).getKey() + ".." + ranges.get(i).getValue() + "\t\t " +
                                address + ", " + nodeID);
        }
        System.out.println();
    }
}
