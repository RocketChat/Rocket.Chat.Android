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
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import chat.rocket.persistence.realm.RealmStore;

public abstract class AbstractOAuthFragment extends AbstractWebViewFragment {

  protected String hostname;
  private String url;
  private boolean resultOK;

  protected abstract String getOAuthServiceName();

  protected abstract String generateURL(RealmMeteorLoginServiceConfiguration oauthConfig);

  private boolean hasValidArgs(Bundle args) {
    return args != null
        && args.containsKey("hostname");
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
          "hostname required");
    }

    hostname = args.getString("hostname");
    RealmMeteorLoginServiceConfiguration oauthConfig =
        RealmStore.get(hostname).executeTransactionForRead(realm ->
            realm.where(RealmMeteorLoginServiceConfiguration.class)
                .equalTo(RealmMeteorLoginServiceConfiguration.SERVICE, getOAuthServiceName())
                .findFirst());
    if (oauthConfig == null) {
      throw new IllegalArgumentException(
          "Invalid hostname given,");
    }
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
    new MethodCallHelper(getContext(), hostname)
        .loginWithOAuth(credentialToken, credentialSecret)
        .continueWith(new LogIfError());
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
