package chat.rocket.android.fragment.oauth;

import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;

public class TwitterOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "twitter";
  }

  @Override
  protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    return "https://" + hostname + "/_oauth/twitter/"
        + "?requestTokenAndRedirect=true&state=" + getStateString();
  }
}
