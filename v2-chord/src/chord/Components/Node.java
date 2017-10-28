package chord.Components;

import chord.Runnable.Listener;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Node implements Serializable {

    private static int m = 7;
    private static int chordRingSize = (int) Math.pow(2, m);

    private InetSocketAddress address = null;
    private Node predecessor = null;
    private Node successor = null;
    private FingerTable fingerTable = null;

    private String addressString = "";
    private long nodeId = -1;
    private String nodeName = "";

    private Listener listener;

    public Node(InetSocketAddress address) {
        try {
            this.address = address;

            // Initialize nodeId and nodeName
            addressString = address.getAddress().getHostAddress() + ":" + address.getPort();
            generateIdAndName(addressString);

            // initialize threads
            listener = new Listener(this);

            //        stabilize = new Stabilize(this);
//        fix_fingers = new FixFingers(this);
//        ask_predecessor = new AskPredecessor(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Node(InetSocketAddress address, long nodeId, String nodeName) {
        try {
            this.address = address;
            this.nodeId = nodeId;
            this.nodeName = nodeName;
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
            long hashValue = Utils.hashAddress(addressString);
            nodeId = hashValue;
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
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new Chord network
     * @return true if successfully creating new network. Otherwise, false
     */
    public boolean createNewNetwork() {
        try {
            System.out.println("Creating new network with 1 node (ID=" + nodeId + ") ...");
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
     * Join the ring in the Chord network from the given contact address
     * @param contactAddress
     * @return true if successfully joining the network. Otherwise, false
     */
    public boolean joinNetwork(InetSocketAddress contactAddress) {
        try {
//            System.out.println("join Network from contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
//            System.out.println("My address: " + address.getAddress().getHostAddress() + ":" + address.getPort());
            if (contactAddress == null || (contactAddress.getAddress().getHostAddress().equals(address.getAddress().getHostAddress()) && contactAddress.getPort() == address.getPort())) {
                System.out.println("Can't join my own address");
                return false;
            }

            // Check if nodeId already exists in the network
            // If nodeId already exists, continue generating and checking new nodeId until nodeId doesn't exist
//            System.out.println("Check if nodeId exists");
            while(Utils.sendMessage(contactAddress, "DOES.ID.EXIST_" + nodeId).equals("ALREADY.EXIST")) {
                System.out.println("ID " + nodeId + "already exists, generating new ID...");
                addressString += "." + address.getPort();
                generateIdAndName(addressString);
            }

            // joining the network
            // find my new successor node
            System.out.println("Joining the network via contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
            successor = findMyNewSuccessor(contactAddress);
            if (successor == null) {
                System.out.println("Can't find my new successor in the network");
                return false;
            }

            // notify my new successor that I'm its new predecessor
            System.out.println("My successor: " + successor.getAddress().getAddress().getHostAddress() + ":" + successor.getAddress().getPort());
//            notifyMyNewSuccessor(successor.getAddress());

            // Update finger table
//            System.out.println("Asking the contact to fill out the finger table");

            // update successor and predecessor in the finger table


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Find my new successor from my contact address
     * @param contactAddress
     * @return new successor node
     */
    public Node findMyNewSuccessor(InetSocketAddress contactAddress) {
        try {
            if (contactAddress == null || (contactAddress.getAddress().getHostAddress().equals(address.getAddress().getHostAddress()) && contactAddress.getPort() == address.getPort())) {
                System.out.println("Can't connect to my contact! wrong contact information");
                return null;
            } else {

                // Response is a Node
                Node newSuccessor = (Node) Utils.sendMessage(contactAddress, "JOINING-FIND.MY.SUCCESSOR_" + this.nodeId);
                System.out.println("findMyNewSuccessor-response: id=" + newSuccessor.getNodeId() + ", address:" + newSuccessor.getAddress().getAddress().getHostAddress() + ":" + newSuccessor.getAddress().getPort());

                return newSuccessor;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Current node helps find ID's successor.
     * @param ID
     * @return ID's successor node
     */
    public Node findSuccessorOf(long ID) {
        try {
            Node successor = this.getSuccessor();

            // Find predecessor of ID
            Node predecessor = findPredecessorOf(ID);

            // if other node found, ask it for its successor
            if (predecessor.getNodeId() != this.nodeId) {
                 // Find successor of predecessor of ID
                // Successor of ID = current successor of predecessor of ID
//                String response = Utils.sendMessage(predecessor.getAddress(), "GET.YOUR.SUCCESSOR");
                successor = (Node) Utils.sendMessage(predecessor.getAddress(), "GET.YOUR.SUCCESSOR");

//                 successor = Utils.constructNodeFrom(response);
            }

            if (successor == null) {
                System.out.println("Can't find " + ID + "'s successor. I'll be your successor");
                successor = this;
            }

            return successor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Current node helps find ID's predecessor.
     * @param ID
     * @return ID's predecessor node
     */
    private Node findPredecessorOf(long ID) {
        try {
            // Start with myself
            Node n = this;

            // ID is not between n and n's successor
            if (n.getSuccessor() != null) {
                while(!(ID >= n.getNodeId() && ID <= n.getSuccessor().getNodeId())) {
                    if (n == null) {
                        System.out.println("Node is null, can't find ID's predecessor");
                        return null;
                    }
                    n = n.closestPrecedingFingerOf(ID);
                }
            }

            return n;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * Find closest preceding finger node of ID
     * @param ID
     * @return closest preceding finger node of ID
     */
    public Node closestPrecedingFingerOf(long ID) {
        try {
            for (int i = Node.getM(); i < 1; i--) {
                Node entryNode = fingerTable.getEntryNode(i);
                if (entryNode != null) {
                    long entryNodeId = entryNode.getNodeId();
                    if (entryNodeId >= this.getNodeId() && entryNodeId <= ID) {
                        return fingerTable.getEntryNode(i);
                    }
                }
            }
            return  null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        long findid_relative = Helper.computeRelativeId(findid, localId);
//
//        // check from last item in finger table
//        for (int i = 32; i > 0; i--) {
//            InetSocketAddress ith_finger = finger.get(i);
//            if (ith_finger == null) {
//                continue;
//            }
//            long ith_finger_id = Helper.hashSocketAddress(ith_finger);
//            long ith_finger_relative_id = Helper.computeRelativeId(ith_finger_id, localId);
//
//            // if its relative id is the closest, check if its alive
//            if (ith_finger_relative_id > 0 && ith_finger_relative_id < findid_relative)  {
//                String response  = Helper.sendRequest(ith_finger, "KEEP");
//
//                //it is alive, return it
//                if (response!=null &&  response.equals("ALIVE")) {
//                    return ith_finger;
//                }
//
//                // else, remove its existence from finger table
//                else {
//                    updateFingers(-2, ith_finger);
//                }
//            }
//        }
//        return localAddress;
    }


    /**
     * Notify my new successor that I'm its new predecessor
     * @param successorAddress
     * @return successor's response
     */
//    public String notifyMyNewSuccessor(InetSocketAddress successorAddress) {
//        try {
//            if (successorAddress == null || (successorAddress.getAddress().getHostAddress().equals(address.getAddress().getHostAddress()) && successorAddress.getPort() == address.getPort())) {
//                System.out.println("Can't notify! empty successor information");
//                return null;
//            } else {
//                System.out.println("Notifying my new successor (" + successorAddress.getAddress().getHostAddress() + ":" + successorAddress.getPort() + ") that I'm (" + address.getAddress().getHostAddress() + ":" + address.getPort() + ") its new predecessor: ");
//                return Utils.sendMessage(successorAddress, "I.AM.YOUR.NEW.PREDECESSOR_" + address.getAddress().getHostAddress() + ":" + address.getPort() + "-" + nodeId + "-" + nodeName);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    /**
     * Stop all threads that have while loop
     */
    public void stopLoopThreads() {
        listener.closeListener();
    }




    /*****************************
     * Getter and setter methods
     */
    public static int getM() {
        return m;
    }

    public static int getChordRingSize() {
        return chordRingSize;
    }

    public InetSocketAddress getAddress() {
        return address;
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

    public Listener getListener() {
        return listener;
    }
}


