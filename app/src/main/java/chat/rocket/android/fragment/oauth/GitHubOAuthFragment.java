package chat.rocket.android.fragment.oauth;

import chat.rocket.core.models.LoginServiceConfiguration;
import okhttp3.HttpUrl;

public class GitHubOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "github";
  }

  @Override
  protected String generateURL(LoginServiceConfiguration oauthConfig) {
    return new HttpUrl.Builder().scheme("https")
        .host("github.com")
        .addPathSegment("login")
        .addPathSegment("oauth")
        .addPathSegment("authorize")
        .addQueryParameter("client_id", oauthConfig.getKey())
        .addQueryParameter("scope", "user:email")
        .addQueryParameter("state", getStateString())
        .build()
        .toString();
  }
}
