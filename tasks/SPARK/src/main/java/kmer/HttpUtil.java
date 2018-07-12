package kmer;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

class RetryCycleException extends Exception {
    public RetryCycleException(String s) {
        super(s);
    }
}

public class HttpUtil {

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException, InterruptedException, RetryCycleException {
        InputStream is = readLocationFromUrl(url).getInputStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject jsonRequest(String url) throws IOException, InterruptedException {
        Optional<JSONObject> maybeJSON = Retriable.runWithRetries(
                3,
                2000,
                () -> readJsonFromUrl(url)
        );
        if (!maybeJSON.isPresent()) {
            throw new IOException("GET Could not be retreived.");
        }
        return maybeJSON.get();
    }

    public static HttpURLConnection readLocationFromUrl(String url) throws RetryCycleException, IOException, InterruptedException {
        String location = url;
        HttpURLConnection connection = null;
        int code = -1;
        int attempts = 0;
        while (true) {
            // attempt the request
            URL currentUrl = new URL(location);
            connection = (HttpURLConnection) currentUrl.openConnection();
            // HttpURLConnection does not respect the `retry-after` header when it follows redirects
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();
            code = connection.getResponseCode();

            // if this is not a 301, return the connection
            if (code != 301) break;

            // else wait until retryAfter seconds is up
            int retryAfter = Integer.parseInt(connection.getHeaderField("retry-after"));
            location = connection.getHeaderField("location");
            System.out.println(String.format("301, retry-after %ds: %s", retryAfter, url));
            Thread.sleep(retryAfter * 1000L); // TODO: this could be more efficient
            attempts++;
            if (attempts >= 10) {
                throw new RetryCycleException("Caught in a HTTP 301 cycle!");
            }
        }
        return connection;
    }

    protected static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
