package centralizedP2P.RunnableThread;

import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable{

    private java.net.ServerSocket serverSocket = null;
    private static boolean isRunning = true;

    public Listener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            System.out.println("Listener running");

            System.out.println("Waiting for a connection...");
            while (isRunning) {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new Server(socket));
                t.start();
            }
            System.out.println("Listener closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeListener() {isRunning = false;}
}