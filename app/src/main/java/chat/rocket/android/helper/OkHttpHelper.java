package chat.rocket.android.helper;

import android.content.Context;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.CookieInterceptor;
import chat.rocket.android.api.rest.DefaultCookieProvider;
import okhttp3.OkHttpClient;

/**
 * Helper class for OkHttp client.
 */
public class OkHttpHelper {
  private static OkHttpClient httpClientForUploadFile;
  private static OkHttpClient httpClientForDownloadFile;
  private static OkHttpClient httpClientForWS;

  public static OkHttpClient getClientForDownloadFile(Context context) {
    if (httpClientForDownloadFile == null) {
      httpClientForDownloadFile = new OkHttpClient.Builder()
          .addNetworkInterceptor(new StethoInterceptor())
          .addInterceptor(
              new CookieInterceptor(new DefaultCookieProvider(new RocketChatCache(context))))
          .build();
    }
    return httpClientForDownloadFile;
  }

  public static OkHttpClient getClientForUploadFile() {
    if (httpClientForUploadFile == null) {
      httpClientForUploadFile = new OkHttpClient.Builder()
          .addNetworkInterceptor(new StethoInterceptor())
          .build();
    }
    return httpClientForUploadFile;
  }

  /**
   * acquire OkHttpClient instance for WebSocket connection.
   */
  public static OkHttpClient getClientForWebSocket() {
    if (httpClientForWS == null) {
      httpClientForWS = new OkHttpClient.Builder().readTimeout(0, TimeUnit.NANOSECONDS)
          .addNetworkInterceptor(new StethoInterceptor())
          .build();
    }
    return httpClientForWS;
  }
}