package chat.rocket.android.layouthelper.oauth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.fragment.oauth.AbstractOAuthFragment;
import chat.rocket.android.fragment.oauth.FacebookOAuthFragment;
import chat.rocket.android.fragment.oauth.GitHubOAuthFragment;
import chat.rocket.android.fragment.oauth.GoogleOAuthFragment;
import chat.rocket.android.fragment.oauth.TwitterOAuthFragment;

/**
 * ViewData model for OAuth login button.
 */
public class OAuthProviderInfo {
  private static final ArrayList<OAuthProviderInfo> _LIST = new ArrayList<OAuthProviderInfo>() {
    {
      add(new OAuthProviderInfo(
          "twitter", R.id.btn_login_with_twitter, TwitterOAuthFragment.class));
      add(new OAuthProviderInfo(
          "github", R.id.btn_login_with_github, GitHubOAuthFragment.class));
      add(new OAuthProviderInfo(
          "google", R.id.btn_login_with_google, GoogleOAuthFragment.class));
      add(new OAuthProviderInfo(
          "facebook", R.id.btn_login_with_facebook, FacebookOAuthFragment.class));
    }
  };
  public static final List<OAuthProviderInfo> LIST = Collections.unmodifiableList(_LIST);
  public String serviceName;
  public int buttonId;
  public Class<? extends AbstractOAuthFragment> fragmentClass;

  /**
   * Constructor with required parameters.
   */
  private OAuthProviderInfo(String serviceName, int buttonId,
                            Class<? extends AbstractOAuthFragment> fragmentClass) {
    this.serviceName = serviceName;
    this.buttonId = buttonId;
    this.fragmentClass = fragmentClass;
  }
}
