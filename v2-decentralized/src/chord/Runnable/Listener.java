package chord.Runnable;

import chord.Components.Node;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread that continuously waits for a node to connect, accept node's connection request,
 * and handle the further request to the server thread.
 */
public class Listener implements Runnable, Serializable {

    private Node myNode = null;
    private transient ServerSocket serverSocket = null;
    private static boolean isRunning = true;

    public Listener(Node node) {

        try {
            this.myNode = node;
            this.serverSocket = new ServerSocket(node.getAddress().getPort(), 0, node.getAddress().getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("Listener running");
            System.out.println("Waiting for the client...");

            while (isRunning) {
                Socket server = serverSocket.accept();
//                System.out.println("Connection accepted from " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());
                Thread t = new Thread(new Server(myNode, server));
                t.start();
            }
            System.out.println("Listener closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeListener() {
        System.out.println("Closing listener");
        isRunning = false;
    }

}