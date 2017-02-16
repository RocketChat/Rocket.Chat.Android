package chat.rocket.android.fragment.oauth;

import chat.rocket.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import okhttp3.HttpUrl;

public class FacebookOAuthFragment extends AbstractOAuthFragment {

  @Override
  protected String getOAuthServiceName() {
    return "facebook";
  }

  @Override
  protected String generateURL(RealmMeteorLoginServiceConfiguration oauthConfig) {
    return new HttpUrl.Builder().scheme("https")
        .host("www.facebook.com")
        .addPathSegment("v2.2")
        .addPathSegment("dialog")
        .addPathSegment("oauth")
        .addQueryParameter("client_id", oauthConfig.getAppId())
        .addQueryParameter("redirect_uri", "https://" + hostname + "/_oauth/facebook?close")
        .addQueryParameter("display", "popup")
        .addQueryParameter("scope", "email")
        .addQueryParameter("state", getStateString())
        .build()
        .toString();
  }
}
