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

            // Initialize a finger table
            fingerTable = new FingerTable(this);

            // initialize threads
            listener = new Listener(this);

            //        stabilize = new Stabilize(this);
//        fix_fingers = new FixFingers(this);
//        ask_predecessor = new AskPredecessor(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For testing
    public Node(InetSocketAddress address, long nodeId, String nodeName) {
        try {
            this.address = address;
            this.nodeId = nodeId;
            this.nodeName = nodeName;

            // Initialize a finger table
            fingerTable = new FingerTable(this);

            // initialize threads
            listener = new Listener(this);
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

            this.successor = this;
            this.predecessor = this;

            startThreads();

            System.out.println("Created network. My nodeId=" + nodeId + ", nodeName=" + nodeName);
            this.fingerTable.printFingerTable();
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
            objArray[0] = MessageType.DOES_ID_EXIST;
            objArray[1] = this.nodeId;
            while(Utils.sendMessage(contactAddress, objArray) == MessageType.ALREADY_EXIST) {
                System.out.println("ID " + nodeId + "already exists, generating new ID...");
                addressString += ".";
                generateIdAndName(addressString);
                objArray[1] = this.nodeId;
            }

            /*
             * Joining the network
             */
            // Initialize finger table of local node
            boolean status = initFingerTable(contactAddress);
            if (status == false) {return false;}

            // Update all nodes whose finger tables should refer to local node
            status = updateFingerTableOfOtherNodes();
            if (status == false) {return false;}

            // Transferring keys


            // find my new successor node
//            System.out.println("Joining the network via contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
//            successor = findMyNewSuccessor(contactAddress);
//            if (successor == null) {
//                System.out.println("Can't find my new successor in the network");
//                return false;
//            }

            // notify my new successor that I'm its new predecessor
//            System.out.println("My successor: " + successor.getAddress().getAddress().getHostAddress() + ":" + successor.getAddress().getPort());
//            notifyMyNewSuccessor(successor.getAddress());

            // Update finger table
//            System.out.println("Asking the contact to fill out the finger table");

            // update successor and predecessor in the finger table

            startThreads();

            objArray[0] = MessageType.PRINT_YOUR_FINGER_TABLE;
            Utils.sendMessage(contactAddress, objArray);
            this.fingerTable.printFingerTable();
            System.out.println("\n" + nodeName + " joined network from contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initialize the successor, predecessor and fingers of node.
     * Ask contact, node in the network, to initialize finger table of local node
     * @param contactAddress
     * @return true if successfully initialized finger table. Otherwise, false.
     */
    public boolean initFingerTable(InetSocketAddress contactAddress) {
        try {
            System.out.println("**************************************************************");
            Object[] objArray = new Object[2];

            /*
             * Ask contact to find the entry node of the 1st finger
             * This entry node is also my successor
             */
            objArray[0] = MessageType.FIND_SUCCESSOR;
            objArray[1] = this.fingerTable.getRange(1).getKey();
            System.out.println(nodeName + ": INIT.FINGER.TABLE - FIND SUC: asking contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort() +
                                " to find successor of id=" + objArray[1]);
            this.successor = (Node) Utils.sendMessage(contactAddress, objArray);
            if (this.successor == null) { return false; }
            this.fingerTable.updateEntryNode(1, this.successor);
            System.out.println(nodeName + ": INIT.FINGER.TABLE - FOUND SUCCESSOR OF me and 1st finger: " + this.successor.getNodeName());

            /*
             * Get predecessor of my successor, it's now my predecessor
             */
            objArray[0] = MessageType.GET_YOUR_PREDECESSOR;
            System.out.println(nodeName + ": INIT.FINGER.TABLE - GET PRE: asking contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort() +
                                " to find get its successor");
            this.predecessor = (Node) Utils.sendMessage(contactAddress, objArray);
            if (this.predecessor == null) { return false; }
            System.out.println(nodeName + ": INIT.FINGER.TABLE - FOUND PREDECESSOR OF me: " + this.predecessor.getNodeName());

            /*
             * Notify my new successor that I'm the new predecessor
             */
            System.out.println(nodeName + ": INIT.FINGER.TABLE: notify my new successor " + this.successor.getAddress().getAddress().getHostAddress() + ":" + this.successor.getAddress().getPort() +
                                " that I'm the new predecessor id=" + this.nodeName);
            MessageType response = notifyMyNewSuccessor(this.getSuccessor().getAddress());
            if (response != MessageType.GOT_IT) { return false; }

            /*
             * Update other entries in the finger table
             */
            for (int i=1; i < getM(); i++) {
//                this.fingerTable.printFingerTable();
                // If (i+1)-th finger's start position is between this node and entry node of i-th finger,
                // (i+1)-th finger entry is the successor of this node.
                long iplus1_start = this.fingerTable.getRange(i+1).getKey();
                System.out.println(nodeName + ": INIT.FINGER.TABLE: Updating " + (i+1) + "-th finger entry, ask contact " + contactAddress.getAddress().getHostAddress() + ":" + contactAddress.getPort() +
                        " to find successor of id=" +iplus1_start);
                //if (iplus1_start >= this.nodeId && iplus1_start < this.fingerTable.getEntryNode(i).getNodeId()) {
                if (isIdBetweenLowerEq(iplus1_start, this.nodeId, this.fingerTable.getEntryNode(i).getNodeId())) {
                    this.fingerTable.updateEntryNode(i+1, this.fingerTable.getEntryNode(i));

                // Otherwise, (i+1)-th finger entry is the successor of (i+1)-th finger's start position
                } else {
                    // Ask contact to find successor of (i+1)-th finger's start position
                    objArray[0] = MessageType.FIND_SUCCESSOR;
                    objArray[1] = iplus1_start;
                    Node n = (Node) Utils.sendMessage(contactAddress, objArray);

                    // Update (i+1)-th finger entry
                    this.fingerTable.updateEntryNode(i+1, n);
                }
            }
            System.out.println(nodeName + " - Successfully initialized finger table!\n");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update the finger tables and predecessors of existing nodes to reflect the addition of this new local node
     *
     * This local node will become the i-th finger of a node p if and only if
     * (1) p precedes this local node by at least 2^(i-1), and
     * (2) the i-th finger of node p succeeds this local node.
     *
     * @return true if successfully update other nodes. Otherwise, false.
     */
    public boolean updateFingerTableOfOtherNodes() {
        try {
            System.out.println("**************************************************************");
            this.fingerTable.printFingerTable();
            for (int i=1; i <= getM(); i++) {
                System.out.println();
                // find last node p whose i-th finger might be this local node
                long id = this.nodeId - (long) Math.pow(2, i-1);
                System.out.println(nodeName + ": UPDATE.OTHER.NODES: i=" + i + ", id=" + id);
                Node p = findPredecessorOf(id);
                if (p != null && p.getNodeId() != this.nodeId) {
                    // Update i-th finger in the finger table of p
                    Object[] msgArray = new Object[2];
                    msgArray[0] = i;
                    msgArray[1] = this;
                    System.out.println(nodeName + ": UPDATE.OTHER.NODES: Updating " + i + "-th finger of node " +
                            p.getNodeName() + ", " + p.getAddress().getAddress().getHostAddress() + ":" + p.getAddress().getPort() +
                            " with node " + this.nodeName + ", " + this.getAddress().getAddress().getHostAddress() + ":" + this.getAddress().getPort());
                    MessageType response = sendUpdateFingerTableMessage(p.getAddress(), msgArray);
                    if (response != MessageType.GOT_IT) { return false;}
                }
            }
            System.out.println(nodeName + " - Successfully updated finger table of other nodes!\n");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * if  n is i-th finger of this local node, update local node’s finger table with n
     * @param i
     * @param n
     */
    public void updateFingerTable(int i, Node n) {
        try {
            //if (n.getNodeId() >= this.nodeId && n.getNodeId() < this.fingerTable.getEntryNode(i).getNodeId()) {
            if (isIdBetweenLowerEq(n.getNodeId(), this.nodeId, this.fingerTable.getEntryNode(i).getNodeId())) {
                System.out.println(nodeName + " - UPDATE.FINGER.TABLE - Updating " + i + "-th finger with new node: " + n.getNodeName());
                this.fingerTable.updateEntryNode(i, n);

                Node p = this.predecessor; // get first preceding node
                if (p.getNodeId() != n.getNodeId()) {
                    // tell predecessor p to update finger table
                    // Update i-th finger in the finger table of p
                    // p.updateFingerTable(i, n);
                    Object[] msgArray = new Object[2];
                    msgArray[0] = i;
                    msgArray[1] = n;
                    System.out.println(nodeName + " - UPDATE.FINGER.TABLE: Tell to Update " + i + "-th finger of node " +
                            p.getNodeName() + ", " + p.getAddress().getAddress().getHostAddress() + ":" + p.getAddress().getPort() +
                            " with node " + n.nodeName + ", " + n.getAddress().getAddress().getHostAddress() + ":" + n.getAddress().getPort());
                    sendUpdateFingerTableMessage(p.getAddress(), msgArray);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessageType sendUpdateFingerTableMessage(InetSocketAddress address, Object[] msgArray) {
        try {
            Object[] objArray = new Object[2];
            objArray[0] = MessageType.UPDATE_FINGER_TABLE;
            objArray[1] = msgArray;
            return (MessageType) Utils.sendMessage(address, objArray);
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
            if (ID < 0 || ID > chordRingSize) { return null; }

            Node successor = this.getSuccessor();

            // Find predecessor of ID
            Node predecessor = findPredecessorOf(ID);

            // Find successor of predecessor of ID
            // Successor of ID = successor of predecessor of ID
            if (predecessor != null) {
                System.out.println(nodeName + ": FIND SUCCESSOR OF " + ID + " - FOUND PRE " + predecessor.getNodeName() + ". SUC is " + predecessor.getSuccessor().getNodeName());
                if (predecessor.getNodeId() != this.nodeId) {
                    Object[] objArray = new Object[1];
                    objArray[0] = MessageType.GET_YOUR_SUCCESSOR;
                    successor = (Node) Utils.sendMessage(predecessor.getAddress(), objArray);
                }
            }

            System.out.println(nodeName + ": FIND SUCCESSOR OF " + ID + " - FOUND " + successor.getNodeName());
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
            if (ID < 0 || ID > chordRingSize) { return null; }

            System.out.println(nodeName + ": FINDING PREDECESSOR OF " + ID + "...");
            // Start with myself
            Node n = this;

            // ID is not between n and n's successor
            if (n.getSuccessor() != null) {
                while( !isIdBetweenUpperEq(ID, n.getNodeId(), n.getSuccessor().getNodeId()) ) {
                    System.out.println(nodeName + ": FINDING PREDECESSOR OF id=" + ID + ", n.getNodeId()=" + n.getNodeId() + ", n.getSuccessor().getNodeId()=" + n.getSuccessor().getNodeId());
                    if (n == null) {
                        System.out.println(nodeName + ": FIND PREDECESSOR: Node is null, can't find ID's predecessor");
                        return null;
                    }
                    if (n.getNodeId() == n.getSuccessor().getNodeId()) {
                        System.out.println(nodeName + ": FINDING PREDECESSOR OF id=" + ID + ", n is the successor of itself");
                        break;
                    }
//                    if (this.fingerTable.findIthFingerOf(ID) >= this.fingerTable.getTransitionFinger()) {
//                        System.out.println(nodeName + ": FINDING PREDECESSOR OF id=" + ID + ", ID is in the finger over transition finger");
////                        if (n.getSuccessor().getNodeId() == n.getPredecessor().getNodeId()) {
////                            n = n.getSuccessor();
////                        }
//                        break;
//                    }

                    // Ask n to find closest finger preceding ID
                    if (n.getNodeId() == this.nodeId) {
                        n = closestPrecedingFingerOf(ID);
                    } else {
                        Object[] objArray = new Object[2];
                        objArray[0] = MessageType.CLOSEST_PRECEDING_FINGER;
                        objArray[1] = ID;
                        n = (Node) Utils.sendMessage(n.getAddress(), objArray);
                    }
                }
            }
            System.out.println(nodeName + ": FIND PREDECESSOR: FOUND ID=" + ID + " 's predecessor: " + n.getNodeName());
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
            if (ID < 0 || ID > chordRingSize) { return null; }
            System.out.println(nodeName + "- FINDING CLOSEST.PRECEDING.FINGER.OF " + ID);
//            this.fingerTable.printFingerTable();
            for (int i = Node.getM(); i > 1; i--) {
                Node entryNode = fingerTable.getEntryNode(i);
                System.out.println(nodeName + "- CLOSEST.PRECEDING.FINGER.OF " + ID + ", i-th finger=" + i + " , entryNode=" + entryNode.getNodeName());
                if (entryNode != null) {
                    if (isIdBetweenNotEq(entryNode.getNodeId(), this.nodeId, ID)) {
                        Node returnNode = fingerTable.getEntryNode(i);
                        System.out.println(nodeName + "- CLOSEST.PRECEDING.FINGER.OF " + ID + " is " + returnNode.getNodeName() + ", " + returnNode.getAddress().getAddress().getHostAddress() + ":" + returnNode.getAddress().getPort());
                        return returnNode;
                    }
                }
            }
            System.out.println(nodeName + "- CLOSEST.PRECEDING.FINGER.OF " + ID + " is " + nodeName + ", " + address.getAddress().getHostAddress() + ":" + address.getPort());
            return  this;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isIdBetweenUpperEq(long id, long n, long np) {
        try {
            System.out.println(nodeName + " - IS.ID.BETWEENUpperEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id > n && id <= np;
            } else {
                res =  id < n && id <= np || id > n && id >= np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isIdBetweenLowerEq(long id, long n, long np) {
        try {
            System.out.println(nodeName + " - IS.ID.BETWEENLowerEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id >= n && id < np;
            } else {
                res =  id <= n && id < np || id >= n && id > np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isIdBetweenNotEq(long id, long n, long np) {
        try {
            System.out.println(nodeName + " - IS.ID.BETWEENNotEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id > n && id < np;
            } else {
                res =  id < n && id < np || id > n && id > np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Notify my new successor that I'm its new predecessor
     * @param successorAddress
     * @return successor's response
     */
    public MessageType notifyMyNewSuccessor(InetSocketAddress successorAddress) {
        try {
            if (successorAddress == null || (successorAddress.getAddress().getHostAddress().equals(address.getAddress().getHostAddress()) && successorAddress.getPort() == address.getPort())) {
                System.out.println(nodeName + ": Can't notify! empty successor information");
                return null;
            } else {
                System.out.println(nodeName + ": Notifying my new successor (" + successorAddress.getAddress().getHostAddress() + ":" + successorAddress.getPort() + ") that I'm (" + address.getAddress().getHostAddress() + ":" + address.getPort() + ") its new predecessor: ");
                Object[] objArray = new Object[2];
                objArray[0] = MessageType.I_AM_YOUR_NEW_PREDECESSOR;
                objArray[1] = this;
                return (MessageType) Utils.sendMessage(successorAddress, objArray);
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
    public static void setM(int m) {
        Node.m = m;
        Node.chordRingSize = (int) Math.pow(2, m);
    }
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


