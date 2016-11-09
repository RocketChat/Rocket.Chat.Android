package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * subscription model for "meteor_accounts_loginServiceConfiguration".
 */
@SuppressWarnings("PMD.ShortVariable")
public class MeteorLoginServiceConfiguration
    extends RealmObject {
  @PrimaryKey private String id;
  private String serverConfigId;
  private String service;
  private String consumerKey; //for Twitter
  private String appId; //for Facebook
  private String clientId; //for other auth providers

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getServerConfigId() {
    return serverConfigId;
  }

  public void setServerConfigId(String serverConfigId) {
    this.serverConfigId = serverConfigId;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
