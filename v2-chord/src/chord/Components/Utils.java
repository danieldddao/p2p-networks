package chord.Components;

import chord.Controller;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Utils {

    /**
     * Hash the give address string using consistent hashing with Linear congruential generator
     * @param address
     * @return m-bit ID key used for nodeId whose value is not larger than the size of the Chord ring
     * @throws Exception
     */
    public static long hashAddress(String address) throws Exception {
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
     * Send message to the given address and receive a response object
     * @param address
     * @param message
     * @return the response object from the given address
     */
    public static Object sendMessage(InetSocketAddress address, String message) {
        try {
            // Send message to the server via server's address
            Socket socket = new Socket(address.getAddress(), address.getPort());
//            PrintStream printStream = new PrintStream(socket.getOutputStream());
//            printStream.println(message);
//            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

            // Wait 50 millisecs to receive the response
            Thread.sleep(50);

            // Receive response object
//            InputStream inputStream = socket.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String response = reader.readLine();

//            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object response = objectInputStream.readObject();
            objectInputStream.close();

//            printStream.close();
//            inputStream.close();
//            reader.close();

            objectOutputStream.close();
            socket.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get address which contains ip address and port number from a given address String
     * @param addressString
     * @return InetSocketAddress of the host address
     */
    public static InetSocketAddress getInetSocketAddressFrom(String addressString) {
        try {
            System.out.println("Getting InetSocketAddress from " + addressString);
            // split input address into ip address and port number
            String[] splitted = addressString.split(":");

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
     * Construct a node from a given nodeString, which has the following format:
     * ipAddress:port-nodeID-nodeName
     * @param nodeString
     * @return constructed node
     */
    public static Node constructNodeFrom(String nodeString) {
        try {
            // Construct a node from given message
            // Message is in the format: ipAddress:port-nodeID-nodeName
            String[] splitted = nodeString.split("-");
            InetSocketAddress mySucAddress = getInetSocketAddressFrom(splitted[0]);
            if (mySucAddress == null) {
                return  null;
            } else {
                Node node = new Node(mySucAddress, Long.parseLong(splitted[1]), splitted[2]);
                return node;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if the given address string exists, so that user can join the Chord network
     * @return address of the given address string if address string exists. Otherwise, null
     */
    public static InetSocketAddress checkAddressExist(String addressString) {
        try {

            InetSocketAddress address = getInetSocketAddressFrom(addressString);
            if (Controller.available(address)) {
                return null;
            } else {
                return address;
            }
        } catch (Exception e) {
            System.out.println("Given address doesn't exist!");
            return null;
        }
    }

}
