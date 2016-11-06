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
  private ServerConfig serverConfig;
  private String service;
  private String consumerKey; //for Twitter
  private String appId; //for Facebook
  private String clientId; //for other auth providers
}
