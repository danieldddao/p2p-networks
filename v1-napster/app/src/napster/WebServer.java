package napster;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class WebServer {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static String url = "http://0.0.0.0:8000";
    private static String token = "";

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

    public static int addNewBook(String user_ip, String port, String title, String isbn, String author, String location) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url + "/books/new");

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//        urlParameters.add(new BasicNameValuePair("authenticity_token", token));
            urlParameters.add(new BasicNameValuePair("user_ip", user_ip));
            urlParameters.add(new BasicNameValuePair("port_number", port));
            urlParameters.add(new BasicNameValuePair("title", title));
            urlParameters.add(new BasicNameValuePair("isbn", isbn));
            urlParameters.add(new BasicNameValuePair("author", author));
            urlParameters.add(new BasicNameValuePair("location", location));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = client.execute(post);
            System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + post.getEntity());
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

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
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
            return "";
        }
    }

}
