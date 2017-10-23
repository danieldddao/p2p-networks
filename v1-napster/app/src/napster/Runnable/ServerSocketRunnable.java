package napster.Runnable;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketRunnable implements Runnable{

    private ServerSocket serverSocket = null;
    private static boolean isRunning = true;

    public ServerSocketRunnable(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            System.out.println("ServerSocketRunnable running");

            System.out.println("Waiting for the client...");
            while (isRunning) {
                Socket socket = serverSocket.accept();
                Thread t = new Thread(new ServerRunnable(socket));
                t.start();
            }
            System.out.println("ServerSocketRunnable closing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeServerSocket() {isRunning = false;}
}