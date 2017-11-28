package chat.rocket.android.helper;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import io.reactivex.Flowable;

public class ServerPolicyHelper {

  private static final String DEFAULT_HOST = ".rocket.chat";
  private static final String VERSION_PROPERTY = "version";

  public static String enforceHostname(String hostname) {
    if (hostname == null) {
      return "open.rocket.chat";
    }

    return removeTrailingSlash(removeProtocol(enforceDefaultHost(hostname)));
  }

  public static Flowable<ServerValidation> isApiVersionValid(
      @NonNull ServerPolicyApiValidationHelper serverPolicyApiValidationHelper) {
    return serverPolicyApiValidationHelper.getApiVersion()
        .map(serverInfo ->
            new ServerValidation(isValid(serverInfo.getApiInfo()),
                serverInfo.usesSecureConnection()));
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

  private static String removeExtraInvalidChars(String hostname) {
    return hostname.replaceAll("[^\\w|\\.|\\-|/]", "");
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

  public static class ServerInfoResponse {
    private final boolean secureConnection;
    private final JSONObject apiInfo;

    public ServerInfoResponse(boolean secureConnection, JSONObject apiInfo) {
      this.secureConnection = secureConnection;
      this.apiInfo = apiInfo;
    }

    public boolean usesSecureConnection() {
      return secureConnection;
    }

    public JSONObject getApiInfo() {
      return apiInfo;
    }
  }

  public static class ServerValidation {
    private final boolean valid;
    private final boolean secureConnection;

    public ServerValidation(boolean valid, boolean secureConnection) {
      this.valid = valid;
      this.secureConnection = secureConnection;
    }

    public boolean isValid() {
      return valid;
    }

    public boolean usesSecureConnection() {
      return secureConnection;
    }
  }
}
