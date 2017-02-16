package chat.rocket.android.fragment.oauth;

import chat.rocket.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;

public class TwitterOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "twitter";
  }

  @Override
  protected String generateURL(RealmMeteorLoginServiceConfiguration oauthConfig) {
    return "https://" + hostname + "/_oauth/twitter/"
        + "?requestTokenAndRedirect=true&state=" + getStateString();
  }
}
