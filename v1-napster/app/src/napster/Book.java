package napster;

public class Book {
    private int id;
    private String user_ip;
    private int port;
    private String title;
    private String author;
    private String isbn;
    private String location;

    public Book(int id, String user_ip, int port, String title, String author, String isbn, String location) {
        this.id = id;
        this.user_ip = user_ip;
        this.port = port;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int ip) {
        this.id = id;
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
}