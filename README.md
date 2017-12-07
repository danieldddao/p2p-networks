# Implementation of centralized P2P network and decentralized P2P network

This project is the implementation of centralized P2P network (based on Napster network) and decentralized P2P network (based on Chord network) for book sharing.

## Getting Started

These instructions will get you run the project.

### Prerequisites

You will need to install Java Runtime Environment 9 in order to run the client applications (jar files)
http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html

If you want to run a server. You will also need to install Ruby. Server is written using Ruby on Rails
https://www.ruby-lang.org/en/

You might need to disable firewall since application might not able to receive TCP socket connection requests due to firewall blocking TCP socket connection requests.

### Running

Directory jars contains jar files for running client applications

##### Centralized P2P:

* **To create a server (Also make sure the local IP address is reachable):** 
    ```
    rails server -b IP -p PORT
    ```
    where IP: local IP address and PORT: port number
    
    E.g. rails server -b 192.168.10.10 -p 2000


* **To run client application:**

    (After running the application, you will be asked to enter the server's address.)

    (There's a default server that you can use: http://p2p-centralized-server.herokuapp.com)
    ```
    java -jar .../centralized-p2p.jar
    ```

##### Decentralized P2P:

* To run application:

    ```
    java -jar .../centralized-p2p.jar
    ```

## Authors

* **Daniel Dao** - *University of Iowa*

## License

This project is licensed under the MIT License.
