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
            long hashValue = Utils.hashHostAddress(addressString);
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
            listener.start();
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
            while(Utils.sendMessage(contactAddress, "DOES.ID.EXIST_" + nodeId).equals("ALREADY.EXIST")) {
                System.out.println("ID " + nodeId + "already exists, generating new ID...");
                addressString += "." + hostAddress.getPort();
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


            // Update finger table
            System.out.println("Asking the contact to fill out the finger table");

            // update successor and predecessor in the finger table


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ask current node to find ID's successor.
     * @param ID
     * @return ID's successor node
     */
    public Node findSuccessorOf(long ID) {
        try {
            Node successor = this.getSuccessor();

            // Find predecessor
            Node predecessor = findPredecessorOf(ID);

            // if other node found, ask it for its successor
            if (predecessor.getNodeId() != this.nodeId) {
                 // Response is in the format ipAddress:port_nodeID_nodeName
                 String response = Utils.sendMessage(predecessor.getHostAddress(), "GET.YOUR.SUCCESSOR");
                String[] splitted = response.split("_");
                 successor = new Node(Utils.getInetSocketAddressFrom(splitted[0]));
                 successor.setNodeId(Long.parseLong(splitted[1]));
                 successor.setNodeName(splitted[2]);
            }
            if (successor == null) {
                successor = this;
            }

            return successor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private Node findPredecessorOf(long ID) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * Find my new successor from my contact host address
     * @param contact
     * @return new successor node
     */
    public Node findMyNewSuccessor(InetSocketAddress contact) {
        try {
            if (contact == null || (contact.getAddress().getHostAddress().equals(hostAddress.getAddress().getHostAddress()) && contact.getPort() == hostAddress.getPort())) {
                System.out.println("Can't connect to my contact! wrong contact information");
                return null;
            } else {

                // Response is in the format: ipAddress:host_nodeID_nodeName-suc's suc-ipAddress:host_nodeID_nodeName
                String response = Utils.sendMessage(contact, "JOINING-FIND.MY.SUCCESSOR_" + hostAddress.getAddress().getHostAddress() + ":" + hostAddress.getPort());

                String[] splitted = response.split("-");

                // Construct my new successor's successor node
                String[] mySucSuc = splitted[1].split("_");
                InetSocketAddress mySucSucHostAddress = Utils.getInetSocketAddressFrom(mySucSuc[0]);
                Node mySucSucNode = new Node(mySucSucHostAddress);
                mySucSucNode.setNodeId(Long.parseLong(mySucSuc[1]));
                mySucSucNode.setNodeName(mySucSuc[2]);

                // Construct my new successor node
                String[] mySuc = splitted[0].split("_");
                InetSocketAddress mySucHostAddress = Utils.getInetSocketAddressFrom(mySuc[0]);
                Node mySucNode = new Node(mySucHostAddress);
                mySucNode.setNodeId(Long.parseLong(mySuc[1]));
                mySucNode.setNodeName(mySuc[2]);
                mySucNode.setPredecessor(this);
                mySucNode.setSuccessor(mySucSucNode);

                return mySucNode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Notify my new successor that I'm its new predecessor
     * @param successor
     * @return successor's response
     */
    public String notifyMyNewSuccessor(InetSocketAddress successor) {
        if (successor == null || (successor.getAddress().getHostAddress().equals(hostAddress.getAddress().getHostAddress()) && successor.getPort() == hostAddress.getPort())) {
            System.out.println("Can't notify! empty successor information");
            return null;
        } else {
            return Utils.sendMessage(successor, "I.AM.YOUR.NEW.PREDECESSOR_" + hostAddress.getAddress().getHostAddress() + ":" + hostAddress.getPort());
        }
    }

    public void stopLoopThreads() {
        listenerRunnable.closeListener();
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


