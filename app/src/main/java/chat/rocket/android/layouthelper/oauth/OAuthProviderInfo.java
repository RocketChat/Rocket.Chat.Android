package chat.rocket.android.layouthelper.oauth;

import chat.rocket.android.R;
import chat.rocket.android.fragment.oauth.AbstractOAuthFragment;
import chat.rocket.android.fragment.oauth.GitHubOAuthFragment;
import chat.rocket.android.fragment.oauth.GoogleOAuthFragment;
import chat.rocket.android.fragment.oauth.TwitterOAuthFragment;
import java.util.ArrayList;

public class OAuthProviderInfo {
  public String serviceName;
  public int buttonId;
  public Class<? extends AbstractOAuthFragment> fragmentClass;

  public OAuthProviderInfo(String serviceName, int buttonId,
      Class<? extends AbstractOAuthFragment> fragmentClass) {
    this.serviceName = serviceName;
    this.buttonId = buttonId;
    this.fragmentClass = fragmentClass;
  }

  public static ArrayList<OAuthProviderInfo> LIST = new ArrayList<OAuthProviderInfo>() {
    {
      add(new OAuthProviderInfo(
          "twitter", R.id.btn_login_with_twitter, TwitterOAuthFragment.class));
      add(new OAuthProviderInfo(
          "github", R.id.btn_login_with_github, GitHubOAuthFragment.class));
      add(new OAuthProviderInfo(
          "google", R.id.btn_login_with_google, GoogleOAuthFragment.class));
    }
  };
}
