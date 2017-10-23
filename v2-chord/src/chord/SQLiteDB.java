package chord;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SQLiteDB {
//
//    private static String myDb = "mybooks.db";
//
//    public static void setupDB() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "CREATE TABLE IF NOT EXISTS BOOK " +
//                    "(PORT INT PRIMARY KEY    NOT NULL," +
//                    " TITLE       TEXT        NOT NULL, " +
//                    " AUTHOR      TEXT        NOT NULL, " +
//                    " ISBN        TEXT, " +
//                    " LOCATION    TEXT        NOT NULL," +
//                    " SHARED      INTEGER     DEFAULT 0)";
//            stmt.executeUpdate(sql);
//            stmt.close();
//            c.close();
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void addNewBook(Book newBook) {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "INSERT INTO BOOK (PORT,TITLE,AUTHOR,ISBN,LOCATION, SHARED) VALUES ('" +
//                            newBook.getPort() + "', '" +
//                            newBook.getTitle() + "', '" +
//                            newBook.getAuthor() + "', '" +
//                            newBook.getIsbn() + "', '" +
//                            newBook.getLocation() + "', '" +
//                            "1');";
//            int status = stmt.executeUpdate(sql);
//            if (status > 0) {
//                System.out.println("Inserted successfully");
//            } else {
//                System.out.println("Can't insert to the db");
//            }
//            stmt.close();
//            c.commit();
//            c.close();
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }
//    }
//
//    public static List<Book> getAllBooks() {
//        List<Book> bookList = new ArrayList();
//
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "SELECT * FROM BOOK;";
//            ResultSet rs = stmt.executeQuery( sql);
//
//            while ( rs.next() ) {
//                int port = rs.getInt("PORT");
//                String  title = rs.getString("TITLE");
//                String author  = rs.getString("AUTHOR");
//                String  isbn = rs.getString("ISBN");
//                String location = rs.getString("LOCATION");
//                int sharedValue = rs.getInt("SHARED");
//
//                Boolean isShared = false;
//                if (sharedValue > 0) { isShared = true;}
//
//                Book newBook = new Book(0, "", port, title, author, isbn, location, isShared);
//                bookList.add(newBook);
//            }
//            if (bookList.size() > 0) {
//                System.out.println("Successfully get all books");
//            } else {
//                System.out.println("No book in the database");
//            }
//            stmt.close();
//            c.commit();
//            c.close();
//            return bookList;
//        } catch ( Exception e ) {
//            e.printStackTrace();
//            return bookList;
//        }
//    }
//
//    public static void shareBook(Book book) {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "UPDATE BOOK SET SHARED ='1' WHERE PORT='" + book.getPort() + "';";
//            stmt.executeUpdate(sql);
//
//            stmt.close();
//            c.commit();
//            c.close();
//            System.out.println("Update successfully");
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void unshareAllBooks() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "UPDATE BOOK SET SHARED ='1';";
//            stmt.executeUpdate(sql);
//
//            stmt.close();
//            c.commit();
//            c.close();
//            System.out.println("Update successfully");
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }
//    }
//
//    public static boolean updateBookLocation(Book book) {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            Connection c = DriverManager.getConnection("jdbc:sqlite:" + myDb);
//            c.setAutoCommit(false);
//            System.out.println("Opened database successfully");
//
//            Statement stmt = c.createStatement();
//            String sql = "UPDATE BOOK SET LOCATION ='" + book.getLocation() + "' WHERE PORT='" + book.getPort() + "';";
//            int status = stmt.executeUpdate(sql);
//
//            stmt.close();
//            c.commit();
//            c.close();
//
//            if (status > 0) {
//                System.out.println("Updated successfully");
//                return true;
//            } else {
//                System.out.println("Updated Unsuccessfully");
//                return false;
//            }
//        } catch ( Exception e ) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
