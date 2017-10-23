package chord.Components;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class FingerTable {

    private HashMap<Integer, InetSocketAddress> table = null;

    public FingerTable() {
        this.table = new HashMap();
        for (int i = 1; i <= 32; i++) {
            updateFinger (i, null);
        }
    }

    /**
     * Update ith finger in the finger table
     */
    public void updateFinger(int i, InetSocketAddress address) {
        try {
            table.put(i, address);

//            // if the updated one is successor, notify the new successor
//            if (i == 1 && value != null && !value.equals(localAddress)) {
//                notify(value);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
