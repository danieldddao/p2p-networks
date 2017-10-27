package chord.Components;

import chord.Controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Utils {

    /**
     * Hash the give host address string using consistent hashing with Linear congruential generator
     * @param address
     * @return m-bit ID key used for nodeId whose value is not larger than the size of the Chord ring
     * @throws Exception
     */
    public static long hashHostAddress(String address) throws Exception {
        int hashValue = 0;
        long generator = address.hashCode();
        while (true) {
            generator = 2862933555777941757L * generator + 1;
            int next = (int) ((hashValue + 1) / (((double) ((int) (generator >>> 33) + 1)) / (0x1.0p31)));
            if (next >= 0 && next < Node.getChordRingSize()) {
                hashValue = next;
            } else {
                break;
            }
        }
        return hashValue;
    }

    /**
     * Send message to the given address
     * @param address
     * @param message
     * @return the response message from the given address
     */
    public static String sendMessage(InetSocketAddress address, String message) {
        try {
            // Send message to the server via server's address
            Socket socket = new Socket(address.getAddress(), address.getPort());
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            printStream.println(message);

            // Wait 50 millisecs to receive the response
            Thread.sleep(50);

            // Receive response message
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();

            socket.close();
            printStream.close();
            inputStream.close();
            reader.close();

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get host address which contains ip address and port number from a gien hostAddress String
     * @param hostAddress
     * @return InetSocketAddress of the host address
     */
    public static InetSocketAddress getInetSocketAddressFrom(String hostAddress) {
        try {
            // split input host address into ip address and port number
            String[] splitted = hostAddress.split(":");

            if (splitted.length == 2) {
                InetAddress address = InetAddress.getByName(splitted[0]);
                int portNumber = Integer.parseInt(splitted[1]);

                // Port number below 1024 and above 49150 is not available
                if (portNumber <= 1110 || portNumber > 49150) {
                    return null;
                }

                InetSocketAddress socketAddress = new InetSocketAddress(address, portNumber);
                System.out.println("Created InetSocketAddress: " + address.getHostAddress() + ":" + portNumber);
                return socketAddress;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if the given host address string exists, so that user can join the Chord network
     * @return InetSocketAddress of the given host address if host address exists. Otherwise, null
     */
    public static InetSocketAddress checkHostAddressExist(String hostAddress) {
        try {

            InetSocketAddress socketAddress = getInetSocketAddressFrom(hostAddress);
            if (Controller.available(socketAddress)) {
                return null;
            } else {
                return socketAddress;
            }
        } catch (Exception e) {
            System.out.println("Given host address doesn't exist!");
            return null;
        }
    }

}
