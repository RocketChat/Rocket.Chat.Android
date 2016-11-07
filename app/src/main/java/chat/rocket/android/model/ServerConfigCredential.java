package chat.rocket.android.model;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android_ddp.DDPClientCallback;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfigCredential extends RealmObject {
  public static final String TYPE_EMAIL = "email";
  public static final String TYPE_TWITTER = "twitter";
  public static final String TYPE_GITHUB = "github";

  @PrimaryKey private String id;
  private String type;
  private String credentialToken;
  private String credentialSecret;
  private String username;
  private String hashedPasswd;
  private String errorMessage;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCredentialToken() {
    return credentialToken;
  }

  public void setCredentialToken(String credentialToken) {
    this.credentialToken = credentialToken;
  }

  public String getCredentialSecret() {
    return credentialSecret;
  }

  public void setCredentialSecret(String credentialSecret) {
    this.credentialSecret = credentialSecret;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getHashedPasswd() {
    return hashedPasswd;
  }

  public void setHashedPasswd(String hashedPasswd) {
    this.hashedPasswd = hashedPasswd;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public static boolean hasSecret(ServerConfigCredential credential) {
    if (credential == null) {
      return false;
    }

    final String authType = credential.getType();
    if (TextUtils.isEmpty(authType)) {
      return false;
    }

    if ("github".equals(authType) || "twitter".equals(authType)) {
      return !TextUtils.isEmpty(credential.getCredentialToken())
          && !TextUtils.isEmpty(credential.getCredentialSecret());
    } else if ("email".equals(authType)) {
      return !TextUtils.isEmpty(credential.getUsername())
          && !TextUtils.isEmpty(credential.getHashedPasswd());
    }

    return false;
  }

  public static void logError(final String id, final Exception exception) {
    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfigCredential.class, new JSONObject()
            .put("id", id)
            .put("errorMessage", getErrorMessageFor(exception)))
    ).continueWith(new LogcatIfError());
  }

  private static String getErrorMessageFor(Exception exception) throws JSONException {
    if (exception instanceof DDPClientCallback.RPC.Error) {
      JSONObject error = ((DDPClientCallback.RPC.Error) exception).error;
      if (!error.isNull("message")) {
        return error.getString("message");
      } else {
        return error.toString();
      }
    } else {
      return exception.getMessage();
    }
  }
}
