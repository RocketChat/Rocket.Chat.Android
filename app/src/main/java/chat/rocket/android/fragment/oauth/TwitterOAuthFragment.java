package chat.rocket.android.fragment.oauth;

import android.util.Base64;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import java.nio.charset.Charset;
import org.json.JSONObject;
import timber.log.Timber;

public class TwitterOAuthFragment extends AbstractOAuthFragment {

  @Override protected String getOAuthServiceName() {
    return "twitter";
  }

  @Override protected String generateURL(MeteorLoginServiceConfiguration oauthConfig) {
    try {
      String state = Base64.encodeToString(new JSONObject().put("loginStyle", "popup")
          .put("credentialToken", "twitter" + System.currentTimeMillis())
          .put("isCordova", true)
          .toString()
          .getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);

      return "https://" + hostname + "/_oauth/twitter/?requestTokenAndRedirect=true&state=" + state;
    } catch (Exception exception) {
      Timber.e(exception, "failed to generate Twitter OAUth URL");
    }
    return null;
  }
}
