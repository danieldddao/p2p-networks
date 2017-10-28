package test;

import chord.Components.Node;
import chord.Controller;

public class TestJoinChord {

    public static void main(String[] args) {

        // First Node
        Controller.createSocketWhenAppStarts();
        Node firstNode = Controller.getMyNode();
        firstNode.createNewNetwork(); // Create new network

        // Second Node
        System.out.println("\n\n*****************************************************");
        System.out.println("Second Node\n\n");
        Controller.createSocketWhenAppStarts();
        Node secondNode = Controller.getMyNode();
        secondNode.joinNetwork(firstNode.getAddress()); // Join first node's network

        // Third Node
        System.out.println("\n\n*****************************************************");
        System.out.println("Third Node\n\n");
        Controller.createSocketWhenAppStarts();
        Node thirdNode = Controller.getMyNode();
        thirdNode.joinNetwork(firstNode.getAddress()); // Join first node's network
    }
}
