package chat.rocket.android.model;

import chat.rocket.android.helper.TextUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

public class ServerConfigCredential extends RealmObject {
  @PrimaryKey private String type;
  private String credentialToken;
  private String credentialSecret;
  private String username;
  private String hashedPasswd;

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
}
