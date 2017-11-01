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

            // Initialize a finger table and predecessor
            fingerTable = new FingerTable(this);
            this.predecessor = this;

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
            Object[] objArray = new Object[2];
            objArray[0] = "DOES ID EXIST";
            objArray[1] = this.nodeId;
            while(Utils.sendMessage(contactAddress, objArray).equals("ALREADY EXIST")) {
                System.out.println("ID " + nodeId + "already exists, generating new ID...");
                addressString += "." + address.getPort();
                generateIdAndName(addressString);
                objArray[1] = this.nodeId;
            }

            /*
             * Joining the network
             */
            // Initialize finger table of local node
            initFingerTable(contactAddress);

            // Update all nodes whose finger tables should refer to local node

            // find my new successor node
            System.out.println("Joining the network via contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
            successor = findMyNewSuccessor(contactAddress);
            if (successor == null) {
                System.out.println("Can't find my new successor in the network");
                return false;
            }

            // notify my new successor that I'm its new predecessor
            System.out.println("My successor: " + successor.getAddress().getAddress().getHostAddress() + ":" + successor.getAddress().getPort());
            notifyMyNewSuccessor(successor.getAddress());

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
     * Ask contact to initialize finger table of local node
     * @param contactAddress
     */
    public void initFingerTable(InetSocketAddress contactAddress) {
        try {
            //finger[1].node = contact.find_successor(finger[1].start)
            // Ask contact to find the entry node of the 1st finger
            Object[] objArray = new Object[2];
            objArray[0] = "FIND SUCCESSOR";
            objArray[1] = this.fingerTable.getRange(1).getKey();
            System.out.println("INIT.FINGER.TABLE: ask contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort() + " to find successor of id=" + objArray[1]);
            Node firstFingerEntry = (Node) Utils.sendMessage(contactAddress, objArray);
            this.fingerTable.updateEntryNode(1, firstFingerEntry);

            
        } catch (Exception e) {
            e.printStackTrace();
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
                Object[] objArray = new Object[2];
                objArray[0] = "FIND SUCCESSOR";
                objArray[1] = this.nodeId;
                Node newSuccessor = (Node) Utils.sendMessage(contactAddress, objArray);
                System.out.println("findMyNewSuccessor-response: id=" + newSuccessor.getNodeId() + ", address:" + newSuccessor.getAddress().getAddress().getHostAddress() + ":" + newSuccessor.getAddress().getPort());

                return newSuccessor;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Ask current node to find ID's successor.
     * @param ID
     * @return ID's successor node
     *
     */
    public Node findSuccessorOf(long ID) {
        try {
            Node successor = this.getSuccessor();

            // Find predecessor of ID
            Node predecessor = findPredecessorOf(ID);

            // Find successor of predecessor of ID
            // Successor of ID = successor of predecessor of ID
            if (predecessor != null) {
                return predecessor.getSuccessor();
            }

            // if ID's predecessor is found, ask it for its successor
//                Object[] objArray = new Object[1];
//                objArray[0] = "GET YOUR SUCCESSOR";
//                successor = (Node) Utils.sendMessage(predecessor.getAddress(), objArray);

//            if (successor == null) {
//                System.out.println("Can't find " + ID + "'s successor. I'll be your successor");
//                successor = this;
//            }

            return successor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Ask current node to find ID's predecessor.
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
     * Find closest finger preceding ID
     * @param ID
     * @return closest finger preceding ID
     */
    public Node closestPrecedingFingerOf(long ID) {
        try {
            for (int i = Node.getM(); i < 1; i--) {
                Node entryNode = fingerTable.getEntryNode(i);
                if (entryNode != null) {
                    long entryNodeId = entryNode.getNodeId();
                    if (entryNodeId > this.getNodeId() && entryNodeId < ID) {
                        return fingerTable.getEntryNode(i);
                    }
                }
            }
            return  this;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Notify my new successor that I'm its new predecessor
     * @param successorAddress
     * @return successor's response
     */
    public String notifyMyNewSuccessor(InetSocketAddress successorAddress) {
        try {
            if (successorAddress == null || (successorAddress.getAddress().getHostAddress().equals(address.getAddress().getHostAddress()) && successorAddress.getPort() == address.getPort())) {
                System.out.println("Can't notify! empty successor information");
                return null;
            } else {
                System.out.println("Notifying my new successor (" + successorAddress.getAddress().getHostAddress() + ":" + successorAddress.getPort() + ") that I'm (" + address.getAddress().getHostAddress() + ":" + address.getPort() + ") its new predecessor: ");
                Object[] objArray = new Object[2];
                objArray[0] = "I AM YOUR NEW PREDECESSOR";
                objArray[1] = this;
                return (String) Utils.sendMessage(successorAddress, objArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


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


