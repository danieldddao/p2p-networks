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
            String sql = "CREATE TABLE IF NOT EXISTS BOOK (" +
                    "USER_IP     TEXT        NOT NULL," +
                    "PORT        INT         NOT NULL," +
                    "BOOK_ID     LONG        DEFAULT -1," +
                    "TITLE       TEXT        NOT NULL, " +
                    "AUTHOR      TEXT        NOT NULL, " +
                    "ISBN        TEXT, " +
                    "LOCATION    TEXT        NOT NULL," +
                    "SHARED      INTEGER     DEFAULT 0," +
                    "PRIMARY KEY (USER_IP, PORT, LOCATION)" +
                    ");";
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

    public List<Book> getAllMyBooks(InetSocketAddress myaddress) {
        List<Book> bookList = new ArrayList();

        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");

            Statement stmt = c.createStatement();
            String sql = "SELECT * FROM BOOK WHERE USER_IP='" + myaddress.getAddress().getHostAddress() +
                            "' AND PORT='" + myaddress.getPort() + "';";
            ResultSet rs = stmt.executeQuery( sql);

            while ( rs.next() ) {
                String  title = rs.getString("TITLE");
                String author  = rs.getString("AUTHOR");
                String  isbn = rs.getString("ISBN");
                String location = rs.getString("LOCATION");
                int sharedValue = rs.getInt("SHARED");

                long bookId = -1;
                Boolean isShared = false;
                if (sharedValue > 0) {
                    isShared = true;
                    bookId = rs.getLong("BOOK_ID");
                }
                System.out.println("SQLDB bookId=" + bookId + " sharedValue=" + sharedValue + " ,isShared=" + isShared);
                Book newBook = new Book(bookId, myaddress, title, author, isbn, location, isShared);
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
            String sql = "UPDATE BOOK SET SHARED='" + shared +
                            "', BOOK_ID='" + book.getId() +
                            "' WHERE USER_IP='" + book.getOwnerAddress().getAddress().getHostAddress() +
                            "' AND PORT='" + book.getOwnerAddress().getPort() +
                            "' AND LOCATION='" + book.getLocation() + "';";
            int status = stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
            System.out.println(sql);
            System.out.println("Updated book's share status successfully (" + status + "): " + book.getId() + ", " + book.getTitle() + ", " + book.getLocation());
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
            String sql = "UPDATE BOOK SET SHARED='0';";
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
                            "' AND PORT='" + book.getOwnerAddress().getPort() +
                            "' AND LOCATION='" + book.getLocation() + "';";
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
