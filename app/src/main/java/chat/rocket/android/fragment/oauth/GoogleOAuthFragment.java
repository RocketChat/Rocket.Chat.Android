package chat.rocket.android.fragment.oauth;

import android.util.Base64;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import java.nio.charset.Charset;
import okhttp3.HttpUrl;
import org.json.JSONObject;
import timber.log.Timber;

public class GoogleOAuthFragment extends AbstractOAuthFragment {

  @Override protected String getOAuthServiceName() {
    return "google";
  }

  @Override protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    final String clientId = oauthConfig.getClientId();
    try {
      String state = Base64.encodeToString(new JSONObject().put("loginStyle", "popup")
          .put("credentialToken", "google" + System.currentTimeMillis())
          .put("isCordova", true)
          .toString()
          .getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);

      return new HttpUrl.Builder().scheme("https")
          .host("accounts.google.com")
          .addPathSegment("o")
          .addPathSegment("oauth2")
          .addPathSegment("auth")
          .addQueryParameter("response_type", "code")
          .addQueryParameter("client_id", clientId)
          .addQueryParameter("scope", "profile email")
          .addQueryParameter("redirect_uri", "https://" + hostname + "/_oauth/google?close")
          .addQueryParameter("state", state)
          .build()
          .toString();
    } catch (Exception exception) {
      Timber.e(exception, "failed to generate Google OAUth URL");
    }
    return null;
  }
}
