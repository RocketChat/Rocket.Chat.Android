package chat.rocket.android.fragment.oauth;

import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import okhttp3.HttpUrl;

public class GitHubOAuthFragment extends AbstractOAuthFragment {

  @Override protected String getOAuthServiceName() {
    return "github";
  }

  @Override protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    return new HttpUrl.Builder().scheme("https")
        .host("github.com")
        .addPathSegment("login")
        .addPathSegment("oauth")
        .addPathSegment("authorize")
        .addQueryParameter("client_id", oauthConfig.getClientId())
        .addQueryParameter("scope", "user:email")
        .addQueryParameter("state", getStateString())
        .build()
        .toString();
  }
}
