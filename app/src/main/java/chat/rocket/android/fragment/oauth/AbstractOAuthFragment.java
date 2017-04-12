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
import chat.rocket.core.models.LoginServiceConfiguration;
import chat.rocket.persistence.realm.repositories.RealmLoginServiceConfigurationRepository;

public abstract class AbstractOAuthFragment extends AbstractWebViewFragment
    implements OAuthContract.View {

  private OAuthContract.Presenter presenter;

  protected abstract String getOAuthServiceName();

  protected String hostname;
  private String url;

  private boolean resultOK;

  protected abstract String generateURL(LoginServiceConfiguration oauthConfig);

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

    presenter = new OAuthPresenter(
        new RealmLoginServiceConfigurationRepository(hostname),
        new MethodCallHelper(getContext(), hostname)
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
    presenter.loadService(getOAuthServiceName());
  }

  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  @Override
  protected void navigateToInitialPage(WebView webview) {
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

  @Override
  public void showService(LoginServiceConfiguration oauthConfig) {
    url = generateURL(oauthConfig);

    showWebView();
  }

  @Override
  public void close() {
    finish();
  }

  @Override
  public void showLoginDone() {
    resultOK = true;
    onOAuthCompleted();
  }

  @Override
  public void showLoginError() {
    onOAuthCompleted();
  }

  private void showWebView() {
    if (TextUtils.isEmpty(url)) {
      finish();
      return;
    }

    final WebView webView = getWebview();
    if (webView == null) {
      finish();
      return;
    }

    resultOK = false;
    webView.getSettings().setUserAgentString("Chrome/56.0.0.0 Mobile");
    webView.loadUrl(url);
    webView.addJavascriptInterface(new JSInterface(result -> {
      // onPageFinish is called twice... Should ignore latter one.
      if (resultOK) {
        return;
      }

      presenter.login(result);
    }), "_rocketchet_hook");
  }

  protected void onOAuthCompleted() {
  }

  private interface JSInterfaceCallback {
    void handleResult(@Nullable JSONObject result);
  }

  private static final class JSInterface {
    private final JSInterfaceCallback jsInterfaceCallback;

    JSInterface(JSInterfaceCallback callback) {
      jsInterfaceCallback = callback;
    }

    @JavascriptInterface
    public void handleConfig(String config) {
      try {
        jsInterfaceCallback.handleResult(new JSONObject(config));
      } catch (Exception exception) {
        jsInterfaceCallback.handleResult(null);
      }
    }
  }
}
