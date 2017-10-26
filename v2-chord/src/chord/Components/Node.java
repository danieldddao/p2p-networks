package chord.Components;

import chord.Controller;
import chord.Runnable.ListenerRunnable;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Node {

    private static int m = 7;
    private static int chordRingSize = (int) Math.pow(2, m);

    private InetSocketAddress hostAddress = null;
    private Node predecessor = null;
    private Node successor = null;
    private FingerTable fingerTable = null;

    private String addressString = "";
    private long nodeId = -1;
    private String nodeName = "";

    private Thread listener;
    private ListenerRunnable listenerRunnable;

    public Node (InetSocketAddress address) {
        try {
            hostAddress = address;

            // Initialize nodeId and nodeName
            addressString = hostAddress.getAddress().getHostAddress() + ":" + hostAddress.getPort();
            generateIdAndName(addressString);

            // initialize threads
            listenerRunnable = new ListenerRunnable(this);
            listener = new Thread(listenerRunnable);
//        stabilize = new Stabilize(this);
//        fix_fingers = new FixFingers(this);
//        ask_predecessor = new AskPredecessor(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * generate new hash key for node's ID and node's name from given address string
     * @param addressString
     */
    private void generateIdAndName(String addressString) {
        try {
            long hashKey = hashHostAddress(addressString);
            nodeId = hashKey;
            nodeName = "N" + nodeId;
            System.out.println("nodeId: " + nodeId);
            System.out.println("nodeName: " + nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start listener thread,
     */
    private void startThreads() {
        try {
            listener.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hash the give host address string using consistent hashing with Linear congruential generator
      * @param address
     * @return m-bit ID key used for nodeId whose value is not larger than the size of the Chord ring
     * @throws Exception
     */
    public static long hashHostAddress(String address) throws Exception {
        int hashKey = 0;
        long generator = address.hashCode();
        while (true) {
            generator = 2862933555777941757L * generator + 1;
            int next = (int) ((hashKey + 1) / (((double) ((int) (generator >>> 33) + 1)) / (0x1.0p31)));
            if (next >= 0 && next < chordRingSize) {
                hashKey = next;
            } else {
                break;
            }
        }
        return hashKey;
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

                // Port number below 1024 and above 49150 is not available
                if (portNumber <= 1110 || portNumber > 49150) {
                    return null;
                }

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
     * Create new Chord network
     * @return true if successfully creating new network. Otherwise, false
     */
    public boolean createNewNetwork() {
        try {
            // Initialize a finger table
            fingerTable = new FingerTable(nodeId);

            startThreads();
            System.out.println("Created network. My nodeId=" + nodeId + ", nodeName=" + nodeName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Join the ring in the Chord network from the given contact host address
     * @param contactAddress
     * @return true if successfully joining the network. Otherwise, false
     */
    public boolean joinNetwork(InetSocketAddress contactAddress) {
        try {
//            System.out.println("join Network from contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
//            System.out.println("My host address: " + hostAddress.getAddress().getHostAddress() + ":" + hostAddress.getPort());
            if (contactAddress == null || (contactAddress.getAddress().getHostAddress().equals(hostAddress.getAddress().getHostAddress()) && contactAddress.getPort() == hostAddress.getPort())) {
                System.out.println("Can't join my own address");
                return false;
            }

            // Check if nodeId already exists in the network
            // If nodeId already exists, continue generating and checking new nodeId until nodeId doesn't exist
//            System.out.println("Check if nodeId exists");
            while(sendMessage(contactAddress, "DOES.ID.EXIST_" + nodeId).equals("ALREADY.EXIST")) {
                System.out.println("ID " + nodeId + "already exists, generating new ID...");
                addressString += "." + hostAddress.getPort();
                generateIdAndName(addressString);
            }

            // joining the network and get my successor's host address
            System.out.println("Joining the network via contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
            String response = sendMessage(contactAddress, "JOINING.FIND.MY.SUC_" + nodeId);


            // update successor and predecessor in the finger table


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send message to the given address
     * @param address
     * @param message
     * @return the response message from the given address
     */
    public String sendMessage(InetSocketAddress address, String message) {
        try {
            // Send message to the server via server's address
            Socket socket = new Socket(address.getAddress(), address.getPort());
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(message);

            // Wait 50 millisecs to receive the response
            Thread.sleep(50);

            // Receive response message
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();

            socket.close();
            printStream.close();
            inputStream.close();
            reader.close();

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void stopLoopThreads() {
        listenerRunnable.closeListener();
    }


    public static int getM() {
        return m;
    }

    public static int getChordRingSize() {
        return chordRingSize;
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

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public FingerTable getFingerTable() {
        return fingerTable;
    }

    public Thread getListener() {
        return listener;
    }

    public ListenerRunnable getListenerRunnable() {
        return listenerRunnable;
    }
}


