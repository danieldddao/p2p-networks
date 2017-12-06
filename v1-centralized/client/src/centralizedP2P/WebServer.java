package centralizedP2P;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class WebServer {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static String url = "http://0.0.0.0:8000";
    private static String token = "";

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        WebServer.url = url;
    }

    public static void getTokenFromWebServer() {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            ;
            String tokenString = "<meta name=\"csrf-token\" content=\"";
            // optional default is GET
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = connection.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
//                System.out.println(inputLine);
                if (inputLine.contains(tokenString)) {
//                    System.out.println("token is in line " + inputLine);
                    token = inputLine.substring(inputLine.indexOf(tokenString) + tokenString.length(), inputLine.indexOf("\" />"));
                    System.out.println("token: " + token);
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean connectToServer() {
        try {
            System.out.println("Check if server is available: " + url);
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/connect");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                System.out.println("Server is available");
                return true;
            } else {
                System.out.println("Server is not available");
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Server is not available");
            return false;
        }
    }

    public static int addNewBook(String username, String user_ip, int port, String title, String isbn, String author, String location) {
        try {
//            userIp = user_ip;

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/new");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", username));
            urlParameters.add(new BasicNameValuePair("user_ip", user_ip));
            urlParameters.add(new BasicNameValuePair("port_number", "" + port));
            urlParameters.add(new BasicNameValuePair("title", title));
            urlParameters.add(new BasicNameValuePair("isbn", isbn));
            urlParameters.add(new BasicNameValuePair("author", author));
            urlParameters.add(new BasicNameValuePair("location", location));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            return response.getStatusLine().getStatusCode();
        } catch(Exception e)
        {
            e.printStackTrace();
            return 400;
        }
    }


    public static String searchBook(String searchTerm) {
        try {
            System.out.println("Searching for books, using term " + searchTerm);
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/search");
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("search_term", searchTerm));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("Sending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            StringBuffer result = new StringBuffer();
            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                System.out.println("\n\nResult:\n" + result + "\n\n");
            } else if (response.getStatusLine().getStatusCode() == 204) {
                System.out.println("No Book Found");
            }

            return result.toString();
        } catch(Exception e)
        {
            e.printStackTrace();
            return "[]";
        }
    }


    public static String findAllMySharedBooks(String username) {
        try {
            System.out.println("Getting all books that I'd shared previously ");
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/allmybooks");
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", username));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("Sending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            StringBuffer result = new StringBuffer();
            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
            } else if (response.getStatusLine().getStatusCode() == 204) {
                System.out.println("No Book Found");
                result.append("[]");
            }
            System.out.println(result);
            return result.toString();

        } catch (ClientProtocolException | IllegalArgumentException | HttpHostConnectException e) {
            return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void unshareBooksFromServerWhenExiting(String username) {
        try {
            if (!username.isEmpty()) {
                System.out.println("Unsharing books");
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(url + "/books/unsharebook");
                post.setHeader("User-Agent", USER_AGENT);
                List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
                urlParameters.add(new BasicNameValuePair("username", username));

                post.setEntity(new UrlEncodedFormEntity(urlParameters));
                HttpResponse response = client.execute(post);
                System.out.println("Sending 'POST' request to URL : " + url);
                System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

                logoutFromServer(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean updateBookLocation(Book book) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/updatelocation");
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", book.getUsername()));
            urlParameters.add(new BasicNameValuePair("title", book.getTitle()));
            urlParameters.add(new BasicNameValuePair("author", book.getAuthor()));
            urlParameters.add(new BasicNameValuePair("location", book.getLocation()));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("Sending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBookSharingStatus(Book book, boolean status) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/updatesharingstatus");
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", book.getUsername()));
            urlParameters.add(new BasicNameValuePair("user_ip", book.getUser_ip()));
            urlParameters.add(new BasicNameValuePair("port_number", "" + book.getPort()));
            urlParameters.add(new BasicNameValuePair("location", book.getLocation()));

            String statusString = "false";
            if (status == true) { statusString = "true"; }
            urlParameters.add(new BasicNameValuePair("sharing_status", statusString));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("Sending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * User table
     */

    public static boolean createNewUser(String username, String password) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/users/create");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", username));
            urlParameters.add(new BasicNameValuePair("password", password));
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);

            System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + post.getEntity());
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 201) {
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int loginToServer(String username, String password, InetSocketAddress address) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/users/login");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", username));
            urlParameters.add(new BasicNameValuePair("password", password));
            urlParameters.add(new BasicNameValuePair("user_ip", address.getAddress().getHostAddress()));
            urlParameters.add(new BasicNameValuePair("port_number", address.getPort() + ""));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);

            System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + post.getEntity());
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            return response.getStatusLine().getStatusCode();
        } catch(Exception e) {
            e.printStackTrace();
            return 400;
        }
    }

    public static boolean logoutFromServer(String username) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/users/logout");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("username", username));
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);

            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
