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

    return removeTrailingSlash(removeProtocol(enforceDefaultHost(hostname)));
  }

  public static void isApiVersionValid(@NonNull OkHttpClient client, @NonNull String host,
                                       @NonNull Callback callback) {
    trySecureValidation(client, host, new Callback() {
      @Override
      public void isValid(boolean usesSecureConnection) {
        callback.isValid(usesSecureConnection);
      }

      @Override
      public void isNotValid() {
        callback.isNotValid();
      }

      @Override
      public void onNetworkError() {
        tryInsecureValidation(client, host, callback);
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

  private static String removeTrailingSlash(String hostname) {
    if (hostname.charAt(hostname.length() - 1) != '/') {
      // no need for a regex - just return it
      return hostname;
    }

    return hostname.replaceAll("/+$", "");
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

  private static void trySecureValidation(@NonNull OkHttpClient client, @NonNull String host,
                                          @NonNull Callback callback) {
    Request request;
    try {
      request = createRequest("https://", host);
    } catch (Exception e) {
      callback.isNotValid();
      return;
    }

    validate(request, client, callback, true);
  }

  private static void tryInsecureValidation(@NonNull OkHttpClient client, @NonNull String host,
                                            @NonNull Callback callback) {
    Request request;
    try {
      request = createRequest("http://", host);
    } catch (Exception e) {
      callback.isNotValid();
      return;
    }

    validate(request, client, callback, false);
  }

  private static Request createRequest(@NonNull String protocol, @NonNull String host) {
    return new Request.Builder()
        .url(protocol + host + API_INFO_PATH)
        .get()
        .build();
  }

  private static void validate(@NonNull Request request, @NonNull OkHttpClient client,
                               @NonNull Callback callback, boolean usesSecureConnection) {
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        callback.onNetworkError();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful() || !isValid(response.body())) {
          callback.isNotValid();
          return;
        }

        callback.isValid(usesSecureConnection);
      }
    });
  }

  public interface Callback {
    void isValid(boolean usesSecureConnection);

    void isNotValid();

    void onNetworkError();
  }
}
