package chord.Components;

import chord.Controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
     * Send message array to the given address and receive a response object
     * @param address
     * @param messageArray (first element is a message string)
     * @return the response object from the given address
     */
    public static Object sendMessage(InetSocketAddress address, Object[] messageArray) {
        try {
            // Send message to the server via server's address
            System.out.println("Sending message " + messageArray);
            Socket socket = new Socket(address.getAddress(), address.getPort());

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(messageArray);
            objectOutputStream.flush();

            // Wait 50 millisecs to receive the response
            Thread.sleep(50);

            // Receive response object
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object response = objectInputStream.readObject();

            objectInputStream.close();
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

    /**
     * Check if id > n and id <= np
     * id, n, np are ids of nodes in the Chord ring
     * @param id
     * @param n
     * @param np
     * @return true if id > n and id <= np. Otherwise, false
     */
    public static boolean isIdBetweenUpperEq(long id, long n, long np) {
        try {
            System.out.println(" - IS.ID.BETWEENUpperEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id > n && id <= np;
            } else {
                res =  id < n && id <= np || id > n && id >= np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if id >= n and id < np
     * id, n, np are ids of nodes in the Chord ring
     * @param id
     * @param n
     * @param np
     * @return true if id >= n and id < np. Otherwise, false
     */
    public static boolean isIdBetweenLowerEq(long id, long n, long np) {
        try {
            System.out.println(" - IS.ID.BETWEENLowerEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id >= n && id < np;
            } else {
                res =  id <= n && id < np || id >= n && id > np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if id >= n and id <= np
     * id, n, np are ids of nodes in the Chord ring
     * @param id
     * @param n
     * @param np
     * @return true if id >= n and id <= np. Otherwise, false
     */
    public static boolean isIdBetweenEq(long id, long n, long np) {
        try {
            System.out.println(" - IS.ID.BETWEENEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id >= n && id <= np;
            } else {
                res =  id <= n && id <= np || id >= n && id >= np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if id > n and id < np
     * id, n, np are ids of nodes in the Chord ring
     * @param id
     * @param n
     * @param np
     * @return true if id > n and id < np. Otherwise, false
     */
    public static boolean isIdBetweenNotEq(long id, long n, long np) {
        try {
            System.out.println(" - IS.ID.BETWEENNotEq id=" + id + " , n=" + n + " , np=" + np);
            boolean res;
            if (n < np) {
                res =  id > n && id < np;
            } else {
                res =  id < n && id < np || id > n && id > np;
            }
            System.out.println(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
