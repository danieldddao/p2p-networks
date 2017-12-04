package test;

import decentralized.Components.Node;
import decentralized.Controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TestBase {
    public static void createSocket(long id, String name) {
        try {
            int initialPort = 1111;
            InetSocketAddress myAddress = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);
            while (!Controller.available(myAddress)) {
                initialPort += 1;
//                System.out.println("Checking port #" + initialPort);
                myAddress = new InetSocketAddress(InetAddress.getLocalHost(), initialPort);
            }
            Controller.setMyNode(new Node(myAddress, id, name));
            System.out.println("Socket created on " + myAddress.getAddress().getHostAddress() + ":" + myAddress.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
