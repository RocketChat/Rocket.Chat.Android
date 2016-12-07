package chat.rocket.android.fragment.oauth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.AbstractWebViewFragment;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import chat.rocket.android.realm_helper.RealmStore;
import hugo.weaving.DebugLog;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class TwitterOAuthFragment extends AbstractWebViewFragment {

  private String serverConfigId;
  private String hostname;
  private String url;
  private boolean resultOK;

  /**
   * create new Fragment with ServerConfig-ID.
   */
  public static TwitterOAuthFragment create(final String serverConfigId) {
    Bundle args = new Bundle();
    args.putString("serverConfigId", serverConfigId);
    TwitterOAuthFragment fragment = new TwitterOAuthFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private boolean hasValidArgs(Bundle args) {
    return args != null
        && args.containsKey("serverConfigId");
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (!hasValidArgs(args)) {
      throw new IllegalArgumentException(
          "serverConfigId required");
    }

    serverConfigId = args.getString("serverConfigId");
    ServerConfig serverConfig = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo("serverConfigId", serverConfigId).findFirst());
    MeteorLoginServiceConfiguration oauthConfig =
        RealmStore.get(serverConfigId).executeTransactionForRead(realm ->
            realm.where(MeteorLoginServiceConfiguration.class)
                .equalTo("service", "twitter")
                .findFirst());
    if (serverConfig == null || oauthConfig == null) {
      throw new IllegalArgumentException(
          "Invalid serverConfigId given,");
    }
    hostname = serverConfig.getHostname();
    url = generateURL();
  }

  private String generateURL() {
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

  @Override protected void navigateToInitialPage(WebView webview) {
    if (TextUtils.isEmpty(url)) {
      finish();
      return;
    }

    resultOK = false;
    webview.loadUrl(url);
    webview.addJavascriptInterface(new JSInterface(result -> {
      // onPageFinish is called twice... Should ignore latter one.
      if (resultOK) {
        return;
      }

      if (result != null && result.optBoolean("setCredentialToken", false)) {
        try {
          final String credentialToken = result.getString("credentialToken");
          final String credentialSecret = result.getString("credentialSecret");

          handleOAuthCallback(credentialToken, credentialSecret);
          resultOK = true;
        } catch (JSONException exception) {
          Timber.e(exception, "failed to parse OAuth result.");
        }
      }

      onOAuthCompleted();
    }), "_rocketchet_hook");
  }

  @DebugLog
  @Override protected void onPageLoaded(WebView webview, String url) {
    super.onPageLoaded(webview, url);


    if (url.contains(hostname) && url.contains("_oauth/twitter?close")) {
      final String jsHookUrl = "javascript:"
          + "window._rocketchet_hook.handleConfig(document.getElementById('config').innerText);";
      webview.loadUrl(jsHookUrl);
    }
  }

  private interface JSInterfaceCallback {
    void hanldeResult(@Nullable JSONObject result);
  }

  private static final class JSInterface {
    private final JSInterfaceCallback jsInterfaceCallback;

    JSInterface(JSInterfaceCallback callback) {
      jsInterfaceCallback = callback;
    }

    @JavascriptInterface public void handleConfig(String config) {
      try {
        jsInterfaceCallback.hanldeResult(new JSONObject(config));
      } catch (Exception exception) {
        jsInterfaceCallback.hanldeResult(null);
      }
    }
  }

  private void handleOAuthCallback(final String credentialToken, final String credentialSecret) {
    new MethodCallHelper(getContext(), serverConfigId)
        .loginWithOAuth(credentialToken, credentialSecret)
        .continueWith(new LogcatIfError());
  }

  private void onOAuthCompleted() {

  }
}
