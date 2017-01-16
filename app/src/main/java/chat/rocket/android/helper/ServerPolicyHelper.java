package chat.rocket.android.helper;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import chat.rocket.android.api.rest.ServerPolicyApi;

public class ServerPolicyHelper {

  private static final String DEFAULT_HOST = ".rocket.chat";
  private static final String VERSION_PROPERTY = "version";

  public static String enforceHostname(String hostname) {
    if (hostname == null) {
      return "demo.rocket.chat";
    }

    return removeTrailingSlash(removeProtocol(enforceDefaultHost(hostname)));
  }

  public static void isApiVersionValid(@NonNull ServerPolicyApi serverPolicyApi,
                                       @NonNull Callback callback) {
    trySecureValidation(serverPolicyApi, new Callback() {
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
        tryInsecureValidation(serverPolicyApi, callback);
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

  private static boolean isValid(JSONObject jsonObject) {
    if (jsonObject == null) {
      return false;
    }

    try {
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

  private static void trySecureValidation(@NonNull ServerPolicyApi serverPolicyApi,
                                          @NonNull Callback callback) {
    serverPolicyApi.getApiInfoSecurely(getServerPolicyApiCallback(true, callback));
  }

  private static void tryInsecureValidation(@NonNull ServerPolicyApi serverPolicyApi,
                                            @NonNull Callback callback) {
    serverPolicyApi.getApiInfoInsecurely(getServerPolicyApiCallback(false, callback));
  }

  private static ServerPolicyApi.Callback getServerPolicyApiCallback(boolean isSecureConnection,
                                                                     @NonNull Callback callback) {
    return new ServerPolicyApi.Callback() {
      @Override
      public void onSuccess(JSONObject jsonObject) {
        if (isValid(jsonObject)) {
          callback.isValid(isSecureConnection);
          return;
        }
        callback.isNotValid();
      }

      @Override
      public void onResponseError() {
        callback.isNotValid();
      }

      @Override
      public void onNetworkError() {
        callback.onNetworkError();
      }
    };
  }

  public interface Callback {
    void isValid(boolean usesSecureConnection);

    void isNotValid();

    void onNetworkError();
  }
}
