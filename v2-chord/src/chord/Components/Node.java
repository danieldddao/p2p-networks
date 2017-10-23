package chord.Components;

import chord.Controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Node {

    private static int m = 3;
    private static int chordRingSize = (int) Math.pow(2, m);

    private InetSocketAddress hostAddress = null;
    private Node predecessor = null;
    private Node successor = null;
    private FingerTable fingerTable = null;

    private static long nodeId = -1;

    public Node (InetSocketAddress address) {
        try {
            hostAddress = address;
            long hasdId = hashHostAddress(address);

            // if hashId belongs to another node, rehash the address

            nodeId = hasdId;

            // Initialize a finger table
            fingerTable = new FingerTable();

            // initialize threads
//        listener = new Listener(this);
//        stabilize = new Stabilize(this);
//        fix_fingers = new FixFingers(this);
//        ask_predecessor = new AskPredecessor(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hash the give host address using consistent hashing with Linear congruential generator
     */
    public static long hashHostAddress(InetSocketAddress address) throws Exception {

//            String addressString = address.getAddress().getHostAddress() + ":" + address.getPort();
//            String addressString = "10.0.1.6:1112";
//            long result = Hashing.consistentHash(address.hashCode(), chordRingSize);
//            System.out.println(result);

        int hashResult = 0;
        long generator = address.hashCode();
        while (true) {
            generator = 2862933555777941757L * generator + 1;
            int next = (int) ((hashResult + 1) / (((double) ((int) (generator >>> 33) + 1)) / (0x1.0p31)));
            if (next >= 0 && next < chordRingSize) {
                hashResult = next;
            } else {
                break;
            }
        }
        System.out.println("hashResult: " + hashResult);
        return hashResult;
    }


    /**
     * Check if the given host address string exists, so that user can join the Chord network
     * @return InetSocketAddress of the given host address if host address exists. Otherwise, null
     */
    public static InetSocketAddress getSocketAddressFrom(String hostAddress) {
        try {

            // split input host address into ip address and port number
            String[] splitted = hostAddress.split(":");

            if (splitted.length == 2) {
                InetAddress address = InetAddress.getByName(splitted[0]);
                int portNumber = Integer.parseInt(splitted[1]);

                InetSocketAddress socketAddress = new InetSocketAddress(address, portNumber);
                System.out.println("Created InetSocketAddress: " + address.getHostAddress() + ":" + portNumber);

                if (Controller.available(socketAddress)) {
                    return null;
                } else {
                    return socketAddress;
                }
            }
            else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Given host address doesn't exist!");
            return null;
        }
    }

    /**
     * Join the ring in the Chord network from the given contact host address
     * @param address
     * @return true if successfully joining the network. Otherwise, false
     */

    public boolean joinRingNetwork(InetSocketAddress contactAddress) {
        try {

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public InetSocketAddress getHostAddress() {
        return hostAddress;
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Node predecessor) {
        this.predecessor = predecessor;
    }

    public Node getSuccessor() {
        return successor;
    }

    public void setSuccessor(Node successor) {
        this.successor = successor;
    }

    public static long getNodeId() {
        return nodeId;
    }

    public static void setNodeId(long nodeId) {
        Node.nodeId = nodeId;
    }
}


