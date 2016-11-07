package chat.rocket.android.fragment.oauth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import chat.rocket.android.fragment.AbstractWebViewFragment;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import chat.rocket.android.model.ServerConfig;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import okhttp3.HttpUrl;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class GitHubOAuthWebViewFragment extends AbstractWebViewFragment {

  private String serverConfigId;
  private String credentialId;
  private String hostname;
  private String url;
  private boolean resultOK;

  public static Fragment create(final String serverConfigId) {
    Bundle args = new Bundle();
    args.putString("server_config_id", serverConfigId);
    Fragment fragment = new GitHubOAuthWebViewFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private boolean hasValidArgs(Bundle args) {
    return args != null
        && args.containsKey("server_config_id");
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (!hasValidArgs(args)) {
      throw new IllegalArgumentException(
          "server_config_id required");
    }

    serverConfigId = args.getString("server_config_id");
    ServerConfig serverConfig = RealmHelper.executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo("id", serverConfigId).findFirst());
    MeteorLoginServiceConfiguration oauthConfig = RealmHelper.executeTransactionForRead(realm ->
        realm.where(MeteorLoginServiceConfiguration.class)
            .equalTo("service", "github")
            .equalTo("serverConfig.id", serverConfigId)
            .findFirst());
    if (serverConfig == null || oauthConfig == null) {
      throw new IllegalArgumentException(
          "Invalid server_config_id given,");
    }
    credentialId = serverConfig.getCredential().getId();
    hostname = serverConfig.getHostname();
    url = generateURL(oauthConfig.getClientId());
  }

  private String generateURL(String clientId) {
    try {
      String state = Base64.encodeToString(new JSONObject().put("loginStyle", "popup")
          .put("credentialToken", "github" + System.currentTimeMillis())
          .put("isCordova", true)
          .toString()
          .getBytes(), Base64.NO_WRAP);

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

  @Override protected void onPageLoaded(WebView webview, String url) {
    super.onPageLoaded(webview, url);


    if (url.contains(hostname) && url.contains("_oauth/github?close")) {
      webview.loadUrl(
          "javascript:window._rocketchet_hook.handleConfig(document.getElementById('config').innerText);");
    }
  }

  private interface JSInterfaceCallback {
    void hanldeResult(@Nullable JSONObject result);
  }

  private static final class JSInterface {
    private final JSInterfaceCallback mCallback;

    public JSInterface(JSInterfaceCallback callback) {
      mCallback = callback;
    }

    @JavascriptInterface public void handleConfig(String config) {
      try {
        mCallback.hanldeResult(new JSONObject(config));
      } catch (Exception e) {
        mCallback.hanldeResult(null);
      }
    }
  }

  private void handleOAuthCallback(final String credentialToken, final String credentialSecret) {
    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("id", serverConfigId)
            .put("credential", new JSONObject()
                .put("id", credentialId)
                .put("type", "github")
                .put("credentialToken", credentialToken)
                .put("credentialSecret", credentialSecret))
        )
    ).continueWith(new LogcatIfError());
  }

  private void onOAuthCompleted() {

  }
}
