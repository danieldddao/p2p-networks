package test;

import decentralized.Components.Book;
import decentralized.Components.Node;
import decentralized.Controller;

public class TestSharingBooks {

    public static void main(String[] args) throws Exception{

        Node.setM(3);
        System.out.println("Chord ring size=" + Node.getChordRingSize());

        // First Node
        TestBase.createSocket(0, "N0");
        Node firstNode = Controller.getMyNode();
        firstNode.createNewNetwork(); // Create new network

        // Second Node
        System.out.println("\n\n*****************************************************");
        System.out.println("*****************************************************");
        System.out.println("Second Node\n\n");
        TestBase.createSocket(3, "N3");
        Node secondNode = Controller.getMyNode();
        secondNode.joinNetwork(firstNode.getAddress()); // Join first node's network

        // First Node share a book
        System.out.println("\n\n-----------------------------------------------------");
        firstNode.shareABook("firstBook", "firstAuthor", "", "/User/Book/firstbook.pdf");
        System.out.println("-----------------------------------------------------");
        firstNode.shareABook("secondBook", "firstAuthor", "", "/User/Book/secondbook.pdf");
        System.out.println("-----------------------------------------------------");
        firstNode.shareABook("thirdBook", "firstAuthor", "", "/User/Book/thirdbook.pdf");
        System.out.println("-----------------------------------------------------");

        firstNode.getFingerTable().printFingerTable();
        System.out.println("N0's SUC is " + firstNode.getSuccessor().getNodeName() + ", PRE is " + firstNode.getPredecessor().getNodeName());
        for (Book b : firstNode.getBookList()) {
            System.out.println("N0 - Book " + b.getId() + ": " + b.getTitle() + " - " + b.getAuthor() + " - " + b.getLocation());
        }

        secondNode.getFingerTable().printFingerTable();
        System.out.println("N3's SUC is " + secondNode.getSuccessor().getNodeName() + ", PRE is " + secondNode.getPredecessor().getNodeName());
        for (Book b : secondNode.getBookList()) {
            System.out.println("N3 - Book " + b.getId() + ": " + b.getTitle() + " - " + b.getAuthor() + " - " + b.getLocation());
        }
    }
}
