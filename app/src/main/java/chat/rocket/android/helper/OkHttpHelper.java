package chat.rocket.android.helper;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

/**
 * Helper class for OkHttp client.
 */
public class OkHttpHelper {
  private static OkHttpClient sHttpClientForUplFile;
  private static OkHttpClient sHttpClientForWS;

  public static OkHttpClient getClientForUploadFile() {
    if (sHttpClientForUplFile == null) {
      sHttpClientForUplFile = new OkHttpClient.Builder()
          .addNetworkInterceptor(new StethoInterceptor())
          .build();
    }
    return sHttpClientForUplFile;
  }

  /**
   * acquire OkHttpClient instance for WebSocket connection.
   */
  public static OkHttpClient getClientForWebSocket() {
    if (sHttpClientForWS == null) {
      sHttpClientForWS = new OkHttpClient.Builder().readTimeout(0, TimeUnit.NANOSECONDS)
          .addNetworkInterceptor(new StethoInterceptor())
          .build();
    }
    return sHttpClientForWS;
  }
}
