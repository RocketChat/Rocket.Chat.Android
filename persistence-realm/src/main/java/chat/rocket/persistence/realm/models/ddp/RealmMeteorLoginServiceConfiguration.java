package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import chat.rocket.core.models.LoginServiceConfiguration;

/**
 * subscription model for "meteor_accounts_loginServiceConfiguration".
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmMeteorLoginServiceConfiguration
    extends RealmObject {

  public static final String ID = "_id";
  public static final String SERVICE = "service";
  public static final String CONSUMER_KEY = "consumerKey";
  public static final String APP_ID = "appId";
  public static final String CLIENT_ID = "clientId";

  @PrimaryKey private String _id;
  private String service;
  private String consumerKey; // for Twitter
  private String appId; // for Facebook
  private String clientId; // for other auth providers

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
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

  public LoginServiceConfiguration asLoginServiceConfiguration() {
    return LoginServiceConfiguration.builder()
        .setId(_id)
        .setService(service)
        .setKey(getServiceKey())
        .build();
  }

  private String getServiceKey() {
    if (consumerKey != null) {
      return consumerKey;
    }

    if (appId != null) {
      return appId;
    }

    return clientId;
  }
}
