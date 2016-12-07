package chat.rocket.android.fragment.oauth;

import android.os.Bundle;
import android.util.Base64;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import java.nio.charset.Charset;
import okhttp3.HttpUrl;
import org.json.JSONObject;
import timber.log.Timber;

public class GitHubOAuthFragment extends AbstractOAuthFragment {

  /**
   * create new Fragment with ServerConfig-ID.
   */
  public static GitHubOAuthFragment create(final String serverConfigId) {
    Bundle args = new Bundle();
    args.putString("serverConfigId", serverConfigId);
    GitHubOAuthFragment fragment = new GitHubOAuthFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override protected String getOAuthServiceName() {
    return "github";
  }

  @Override protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    final String clientId = oauthConfig.getClientId();
    try {
      String state = Base64.encodeToString(new JSONObject().put("loginStyle", "popup")
          .put("credentialToken", "github" + System.currentTimeMillis())
          .put("isCordova", true)
          .toString()
          .getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);

      return new HttpUrl.Builder().scheme("https")
          .host("github.com")
          .addPathSegment("login")
          .addPathSegment("oauth")
          .addPathSegment("authorize")
          .addQueryParameter("client_id", clientId)
          .addQueryParameter("scope", "user:email")
          .addQueryParameter("state", state)
          .build()
          .toString();
    } catch (Exception exception) {
      Timber.e(exception, "failed to generate GitHub OAUth URL");
    }
    return null;
  }
}
