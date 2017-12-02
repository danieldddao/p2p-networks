package napster.Runnable;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocket implements Runnable{

    private java.net.ServerSocket serverSocket = null;
    private static boolean isRunning = true;

    public ServerSocket(java.net.ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            System.out.println("ServerSocket running");

            System.out.println("Waiting for the client...");
            while (isRunning) {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new Server(socket));
                t.start();
            }
            System.out.println("ServerSocket closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeServerSocket() {isRunning = false;}
}