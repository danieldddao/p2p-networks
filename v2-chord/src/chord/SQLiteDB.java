package chord;

import chord.Components.Book;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDB {

    private static String myDb = "mySharedBooks.db";

    public SQLiteDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS BOOK " +
                    "(USER_IP TEXT PRIMARY KEY NOT NULL," +
                    "PORT INT PRIMARY KEY    NOT NULL," +
                    "BOOK_ID      LONG     DEFAULT -1)," +
                    "TITLE       TEXT        NOT NULL, " +
                    "AUTHOR      TEXT        NOT NULL, " +
                    "ISBN        TEXT, " +
                    "LOCATION    TEXT PRIMARY KEY NOT NULL," +
                    "SHARED      INTEGER     DEFAULT 0)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void addNewBook(Book newBook) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();
            String sql = "INSERT INTO BOOK (USER_IP,PORT,BOOK_ID,TITLE,AUTHOR,ISBN,LOCATION,SHARED) VALUES ('" +
                            newBook.getOwnerAddress().getAddress().getHostAddress() + "', '" +
                            newBook.getOwnerAddress().getPort() + "', '" +
                            newBook.getId() + "', '" +
                            newBook.getTitle() + "', '" +
                            newBook.getAuthor() + "', '" +
                            newBook.getIsbn() + "', '" +
                            newBook.getLocation() + "', '" +
                            "1');";
            int status = stmt.executeUpdate(sql);
            if (status > 0) {
                System.out.println("Inserted successfully");
            } else {
                System.out.println("Can't insert to the db");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList();

        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM BOOK;";
            ResultSet rs = stmt.executeQuery( sql);

            while ( rs.next() ) {
                String user_ip = rs.getString("USER_IP");
                int port = rs.getInt("PORT");
                String  title = rs.getString("TITLE");
                String author  = rs.getString("AUTHOR");
                String  isbn = rs.getString("ISBN");
                String location = rs.getString("LOCATION");
                int sharedValue = rs.getInt("SHARED");

                Boolean isShared = false;
                if (sharedValue > 0) { isShared = true;}

                InetAddress address = InetAddress.getByName(user_ip);
                InetSocketAddress socketAddress = new InetSocketAddress(address, port);
                Book newBook = new Book(-1, socketAddress, title, author, isbn, location, isShared);
                bookList.add(newBook);
            }

            stmt.close();
            c.commit();
            c.close();
            System.out.println("Successfully get all books");
            return bookList;
        } catch ( Exception e ) {
            e.printStackTrace();
            return bookList;
        }
    }

    public void updateBookShareStatus(Book book) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            int shared = 0;
            if (book.getIsShared()) { shared = 1; }
            Statement stmt = c.createStatement();
            String sql = "UPDATE BOOK SET SHARED ='" + shared +
                            "', BOOK_ID='" + book.getId() +
                            "' WHERE USER_IP='" + book.getOwnerAddress().getAddress().getHostAddress() +
                            "', PORT='" + book.getOwnerAddress().getPort() +
                            "', LOCATION='" + book.getLocation() + "';";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
            System.out.println("Updated book's share status successfully");
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void unshareAllBooks() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();
            String sql = "UPDATE BOOK SET SHARED ='0';";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
            System.out.println("Unshared all books successfully");
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public boolean updateBookLocation(Book book, String newLocation) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();

            int shared = 0;
            if (book.getIsShared()) { shared = 1; }
            String sql = "UPDATE BOOK SET LOCATION='" + newLocation +
                            "', BOOK_ID='" + book.getId() +
                            "', SHARED='" + shared +
                            "' WHERE USER_IP='" + book.getOwnerAddress().getAddress().getHostAddress() +
                            "', PORT='" + book.getOwnerAddress().getPort() +
                            "', LOCATION='" + book.getLocation() + "';";
            int status = stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();

            if (status > 0) {
                System.out.println("Updated book's location successfully");
                return true;
            } else {
                System.out.println("Unsuccessful Updated book's location");
                return false;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }
}
