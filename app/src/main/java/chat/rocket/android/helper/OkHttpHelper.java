package chat.rocket.android.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.CookieInterceptor;
import chat.rocket.android.api.rest.DefaultCookieProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Helper class for OkHttp client.
 */
public class OkHttpHelper {
  private static OkHttpClient defaultHttpClient;
  private static OkHttpClient httpClientForUploadFile;
  private static OkHttpClient httpClientForDownloadFile;
  private static OkHttpClient httpClientForWS;
  private static String contentType;

  public static OkHttpClient getDefaultHttpClient() {
    if (defaultHttpClient == null) {
      defaultHttpClient = new OkHttpClient();
    }
    return defaultHttpClient;
  }

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

  public static String getContentType(String uri) {
    Request request = new Request.Builder()
        .url(uri)
        .head()
        .build();
    getDefaultHttpClient().newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException ioException) {
        ioException.printStackTrace();
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull final Response response)
          throws IOException {
        if (!response.isSuccessful()) {
          throw new IOException("Unexpected code: " + response);
        } else {
          contentType = response.header("Content-Type");
        }
      }
    });
    return contentType;
  }
}
