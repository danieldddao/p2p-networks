package napster;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private int id;
    private String username;
    private String user_ip;
    private int port;
    private String title;
    private String author;
    private String isbn;
    private String location;
    private Boolean isShared;

    public Book(int id, String username, String user_ip, int port, String title, String author, String isbn, String location, Boolean isShared) {
        this.id = id;
        this.username = username;
        this.user_ip = user_ip;
        this.port = port;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.location = location;
        this.isShared = isShared;
    }

    public int getId() {
        return id;
    }

    public void setId(int ip) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip(String user_ip) {
        this.user_ip = user_ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getIsShared() { return isShared; }

    public void setIsShared(Boolean shared) { isShared = shared; }

    public static List<Book> jsonToBookList(String jsonString) {
        List<Book> returnList = new ArrayList();
        try {
            JSONArray jsonarray = new JSONArray(jsonString);
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                int id = jsonobject.getInt("id");
                String username = jsonobject.getString("username");
                String user_ip = jsonobject.getString("user_ip");
                int port = Integer.parseInt(jsonobject.getString("port_number"));
                String title = jsonobject.getString("title");
                String isbn = jsonobject.getString("isbn");
                String author = jsonobject.getString("author");
                String location = jsonobject.getString("location");
                Boolean isShared = jsonobject.getBoolean("isShared");

                Book newBook = new Book(id, username, user_ip, port, title, author, isbn, location, isShared);
                returnList.add(newBook);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            return returnList;
        }
    }

}