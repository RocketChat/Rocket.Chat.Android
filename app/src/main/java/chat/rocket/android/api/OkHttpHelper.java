package chat.rocket.android.api;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHttpHelper {
    private static OkHttpClient sHttpClient = new OkHttpClient();
    public static OkHttpClient getClient() {
        return sHttpClient;
    }

    private static OkHttpClient sHttpClientForWS = new OkHttpClient();
    static{
        sHttpClientForWS.setReadTimeout(0, TimeUnit.NANOSECONDS);
    }
    public static OkHttpClient getClientForWebSocket() {
        return sHttpClientForWS;
    }
}
