package test;

import chord.Components.Book;
import chord.Components.Node;
import chord.Controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class TestSearchBooks {

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

        //Third Node
        System.out.println("\n\n*****************************************************");
        System.out.println("*****************************************************");
        System.out.println("Third Node\n\n");
        TestBase.createSocket(6, "N6");
        Node thirdNode = Controller.getMyNode();
        thirdNode.joinNetwork(firstNode.getAddress()); // Join first node's network

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

        thirdNode.getFingerTable().printFingerTable();
        System.out.println("N6's SUC is " + thirdNode.getSuccessor().getNodeName() + ", PRE is " + thirdNode.getPredecessor().getNodeName());
        for (Book b : thirdNode.getBookList()) {
            System.out.println("N6 - Book " + b.getId() + ": " + b.getTitle() + " - " + b.getAuthor() + " - " + b.getLocation());
        }

        List<Book> result = firstNode.searchBook("firstBook");
        for (Book b : result) {
            System.out.println("N0 - Search Book - result: id=" + b.getId() + " - " + b.getTitle() + " - " + b.getAuthor() + " - " + b.getLocation() + " - " + b.getOwnerAddress().getAddress().getHostAddress() + ":" + b.getOwnerAddress().getPort());
        }
    }
}
