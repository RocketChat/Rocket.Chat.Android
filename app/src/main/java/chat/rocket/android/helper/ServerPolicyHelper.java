package chat.rocket.android.helper;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ServerPolicyHelper {

  private static final String DEFAULT_HOST = ".rocket.chat";
  private static final String API_INFO_PATH = "/api/info";
  private static final String VERSION_PROPERTY = "version";

  public static String enforceHostname(String hostname) {
    if (hostname == null) {
      return "demo.rocket.chat";
    }

    return removeProtocol(enforceDefaultHost(hostname));
  }

  public static void isApiVersionValid(@NonNull OkHttpClient client, @NonNull String host,
                                       @NonNull Callback callback) {
    Request request;
    try {
      request = new Request.Builder()
          .url("https://" + host + API_INFO_PATH)
          .get()
          .build();
    } catch (Exception e) {
      callback.isNotValid();
      return;
    }

    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        // some connection error
        callback.isNotValid();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful() || !isValid(response.body())) {
          callback.isNotValid();
          return;
        }

        callback.isValid();
      }
    });
  }

  @NonNull
  private static String enforceDefaultHost(String hostname) {
    if (hostname.indexOf('.') == -1) {
      hostname = hostname + DEFAULT_HOST;
    }
    return hostname;
  }

  @NonNull
  private static String removeProtocol(String hostname) {
    // yep. cheap.
    return hostname.replace("http://", "").replace("https://", "");
  }

  private static boolean isValid(ResponseBody body) {
    if (body == null || body.contentLength() == 0) {
      return false;
    }

    try {
      final JSONObject jsonObject = new JSONObject(body.string());

      return jsonObject.has(VERSION_PROPERTY)
          && isVersionValid(jsonObject.getString(VERSION_PROPERTY));
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean isVersionValid(String version) {
    if (version == null || version.length() == 0) {
      return false;
    }

    String[] versionParts = version.split("\\.");
    return versionParts.length >= 3 && Integer.parseInt(versionParts[1]) >= 49;
  }

  public interface Callback {
    void isValid();

    void isNotValid();
  }
}
