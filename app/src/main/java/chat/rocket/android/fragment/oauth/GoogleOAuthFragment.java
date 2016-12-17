package chat.rocket.android.fragment.oauth;

import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import okhttp3.HttpUrl;

public class GoogleOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "google";
  }

  @Override
  protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    return new HttpUrl.Builder().scheme("https")
        .host("accounts.google.com")
        .addPathSegment("o")
        .addPathSegment("oauth2")
        .addPathSegment("auth")
        .addQueryParameter("response_type", "code")
        .addQueryParameter("client_id", oauthConfig.getClientId())
        .addQueryParameter("scope", "profile email")
        .addQueryParameter("redirect_uri", "https://" + hostname + "/_oauth/google?close")
        .addQueryParameter("state", getStateString())
        .build()
        .toString();
  }
}
