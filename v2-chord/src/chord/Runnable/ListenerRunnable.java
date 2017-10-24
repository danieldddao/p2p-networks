package chord.Runnable;

import chord.Components.Node;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread that continuously waits for a node to connect, accept node's connection request,
 * and handle the further request to the server thread.
 */
public class ListenerRunnable implements Runnable {

    private Node myNode = null;
    private ServerSocket serverSocket = null;
    private static boolean isRunning = true;

    public ListenerRunnable(Node node) {

        try {
            this.myNode = node;
            this.serverSocket = new ServerSocket(node.getHostAddress().getPort(), 0, node.getHostAddress().getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("ListenerRunnable running");
            System.out.println("Waiting for the client...");

            while (isRunning) {
                Socket server = serverSocket.accept();
                System.out.println("Connection accepted from " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());
                Thread t = new Thread(new ServerRunnable(myNode, server));
                t.start();
            }
            System.out.println("ListenerRunnable closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeListener() {isRunning = false;}

}