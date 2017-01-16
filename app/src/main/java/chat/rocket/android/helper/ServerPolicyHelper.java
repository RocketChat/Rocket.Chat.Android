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
    ServerPolicyApiValidationHelper.getApiVersion(serverPolicyApi,
        new ServerPolicyApiValidationHelper.Callback() {
          @Override
          public void onSuccess(boolean usesSecureConnection, JSONObject apiInfo) {
            if (isValid(apiInfo)) {
              callback.isValid(usesSecureConnection);
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

  public interface Callback {
    void isValid(boolean usesSecureConnection);

    void isNotValid();

    void onNetworkError();
  }
}
