package test;

import decentralizedP2P.Components.Node;
import decentralizedP2P.Controller;

public class TestJoiningChord {


    public static void main(String[] args) throws Exception{

        Node.setM(3);
        System.out.println("Chord ring size=" + Node.getChordRingSize());

        //First Node
        TestBase.createSocket(0, "N0");
        Node firstNode = Controller.getMyNode();
        firstNode.createNewNetwork(); // Create new network

        //Second Node
        System.out.println("\n\n*****************************************************");
        System.out.println("*****************************************************");
        System.out.println("Second Node\n\n");
        TestBase.createSocket(3, "N3");
        Node secondNode = Controller.getMyNode();
        secondNode.joinNetwork(firstNode.getAddress()); // Join first node's network

        //Third Node
        System.out.println("\n\n*****************************************************");
        System.out.println("*****************************************************");
        System.out.println("Third Node\n\n");
        TestBase.createSocket(6, "N6");
        Node thirdNode = Controller.getMyNode();
        thirdNode.joinNetwork(firstNode.getAddress()); // Join first node's network

        //Fourth Node
        System.out.println("\n\n*****************************************************");
        System.out.println("*****************************************************");
        System.out.println("Fourth Node\n\n");
        TestBase.createSocket(1, "N1");
        Node fourthNode = Controller.getMyNode();
        fourthNode.joinNetwork(secondNode.getAddress()); // Join second node's network


        firstNode.getFingerTable().printFingerTable();
        System.out.println(firstNode.getNodeName() + "'s SUC is " + firstNode.getSuccessor().getNodeName() + ", PRE is " + firstNode.getPredecessor().getNodeName());

        secondNode.getFingerTable().printFingerTable();
        System.out.println(secondNode.getNodeName() + "'s SUC is " + secondNode.getSuccessor().getNodeName() + ", PRE is " + secondNode.getPredecessor().getNodeName());

        thirdNode.getFingerTable().printFingerTable();
        System.out.println(thirdNode.getNodeName() + "'s SUC is " + thirdNode.getSuccessor().getNodeName() + ", PRE is " + thirdNode.getPredecessor().getNodeName());

        fourthNode.getFingerTable().printFingerTable();
        System.out.println(fourthNode.getNodeName() + "'s SUC is " + fourthNode.getSuccessor().getNodeName() + ", PRE is " + fourthNode.getPredecessor().getNodeName());

    }
}
