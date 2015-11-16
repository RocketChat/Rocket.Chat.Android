package chat.rocket.android.api;

import com.squareup.okhttp.OkHttpClient;

public class OkHttpHelper {
    private static OkHttpClient sHttpClient = new OkHttpClient();
    public static OkHttpClient getClient() {
        return sHttpClient;
    }
}
