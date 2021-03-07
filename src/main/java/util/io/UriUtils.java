package util.io;

import org.apache.http.client.methods.CloseableHttpResponse;

public class UriUtils {

    //private to prevent instantiations
    private UriUtils() {}
    

    public static boolean isSuccess(CloseableHttpResponse resp) {
        int code = resp.getStatusLine().getStatusCode();
        return code >= 200 && code < 300;
    }
}
