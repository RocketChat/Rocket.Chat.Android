package chat.rocket.android.fragment.oauth;

import chat.rocket.core.models.LoginServiceConfiguration;

public class TwitterOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "twitter";
  }

  @Override
  protected String generateURL(LoginServiceConfiguration oauthConfig) {
    return "https://" + hostname + "/_oauth/twitter/"
        + "?requestTokenAndRedirect=true&state=" + getStateString();
  }
}
