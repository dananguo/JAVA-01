package hw1;

import io.netty.handler.codec.http.HttpHeaders;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyichi006
 * @date 2021/1/26 15:01
 * @description
 */
public class OkHttpUtils {
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .callTimeout(1000, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .build();

    public static void main(String[] args) throws IOException {
        String url = "http://localhost:8888/hello/";
        String text = OkHttpUtils.getAsString(url, null);
        System.out.println("url: " + url + " ; response: \n" + text);

        okHttpClient = null;
    }

    public static String getAsString(String url, HttpHeaders headers) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder.url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String ret = response.body().string();
            System.out.println(ret);
            return new String(ret.getBytes(), StandardCharsets.UTF_8);
        }
    }
}
