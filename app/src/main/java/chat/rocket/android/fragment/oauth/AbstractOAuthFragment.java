package chat.rocket.android.fragment.oauth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.AbstractWebViewFragment;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import chat.rocket.android.realm_helper.RealmStore;

public abstract class AbstractOAuthFragment extends AbstractWebViewFragment {

  protected String serverConfigId;
  protected String hostname;
  private String url;
  private boolean resultOK;

  protected abstract String getOAuthServiceName();

  protected abstract String generateURL(MeteorLoginServiceConfiguration oauthConfig);

  private boolean hasValidArgs(Bundle args) {
    return args != null
        && args.containsKey("serverConfigId");
  }

  protected final String getStateString() {
    try {
      return Base64.encodeToString(new JSONObject().put("loginStyle", "popup")
          .put("credentialToken", getOAuthServiceName() + System.currentTimeMillis())
          .put("isCordova", true)
          .toString()
          .getBytes(Charset.forName("UTF-8")), Base64.NO_WRAP);
    } catch (JSONException exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (!hasValidArgs(args)) {
      throw new IllegalArgumentException(
          "serverConfigId required");
    }

    serverConfigId = args.getString("serverConfigId");
    ServerConfig serverConfig = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo(ServerConfig.ID, serverConfigId).findFirst());
    MeteorLoginServiceConfiguration oauthConfig =
        RealmStore.get(serverConfigId).executeTransactionForRead(realm ->
            realm.where(MeteorLoginServiceConfiguration.class)
                .equalTo(MeteorLoginServiceConfiguration.SERVICE, getOAuthServiceName())
                .findFirst());
    if (serverConfig == null || oauthConfig == null) {
      throw new IllegalArgumentException(
          "Invalid serverConfigId given,");
    }
    hostname = serverConfig.getHostname();
    url = generateURL(oauthConfig);
  }

  @Override
  protected void navigateToInitialPage(WebView webview) {
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
          RCLog.e(exception, "failed to parse OAuth result.");
        }
      }

      onOAuthCompleted();
    }), "_rocketchet_hook");
  }

  @Override
  protected void onPageLoaded(WebView webview, String url) {
    super.onPageLoaded(webview, url);

    if (url.contains(hostname) && url.contains("_oauth/" + getOAuthServiceName() + "?close")) {
      final String jsHookUrl = "javascript:"
          + "window._rocketchet_hook.handleConfig(document.getElementById('config').innerText);";
      webview.loadUrl(jsHookUrl);
    }
  }

  private void handleOAuthCallback(final String credentialToken, final String credentialSecret) {
    new MethodCallHelper(getContext(), serverConfigId)
        .loginWithOAuth(credentialToken, credentialSecret)
        .continueWith(new LogcatIfError());
  }

  protected void onOAuthCompleted() {

  }

  private interface JSInterfaceCallback {
    void hanldeResult(@Nullable JSONObject result);
  }

  private static final class JSInterface {
    private final JSInterfaceCallback jsInterfaceCallback;

    JSInterface(JSInterfaceCallback callback) {
      jsInterfaceCallback = callback;
    }

    @JavascriptInterface
    public void handleConfig(String config) {
      try {
        jsInterfaceCallback.hanldeResult(new JSONObject(config));
      } catch (Exception exception) {
        jsInterfaceCallback.hanldeResult(null);
      }
    }
  }
}
