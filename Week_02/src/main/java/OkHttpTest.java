import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author zhangyichi006
 * @date 2021/1/26 11:18
 * @description
 */
public class OkHttpTest {

    private static OkHttpClient okHttpClient = new OkHttpClient();

    public static void main(String[] args) {
        String url = "http://localhost:8801";
        System.out.println(connect(url));
        okHttpClient = null;
    }

    public static String connect(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            return e.toString();
        }
    }
}
