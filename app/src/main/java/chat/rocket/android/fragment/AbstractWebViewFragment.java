package chat.rocket.android.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import chat.rocket.android.R;
import chat.rocket.android.helper.OnBackPressListener;
import hugo.weaving.DebugLog;

public abstract class AbstractWebViewFragment extends AbstractFragment
    implements OnBackPressListener {
  private WebView webview;
  private WebViewClient webviewClient = new WebViewClient() {
    private boolean error;

    @Override
    public void onPageStarted(WebView webview, String url, Bitmap favicon) {
      error = false;
    }

    @Override
    public void onPageFinished(WebView webview, String url) {
      if (!error) {
        onPageLoaded(webview, url);
      }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
      super.onReceivedError(view, request, error);
      this.error = true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webview, String url) {
      return (shouldOverride(webview, url) && onHandleCallback(webview, url))
          || super.shouldOverrideUrlLoading(webview, url);
    }

    @DebugLog
    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
      //resend POST request without confirmation.
      resend.sendToTarget();
    }
  };

  @Override
  protected int getLayout() {
    return R.layout.webview;
  }

  @Override
  protected void onSetupView() {
    webview = (WebView) rootView.findViewById(R.id.webview);
    setupWebView();

    navigateToInitialPage(webview);
  }

  private void setupWebView() {
    WebSettings settings = webview.getSettings();
    if (settings != null) {
      settings.setJavaScriptEnabled(true);
    }
    webview.setHorizontalScrollBarEnabled(false);
    webview.setWebViewClient(webviewClient);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
      //refs: https://code.google.com/p/android/issues/detail?id=35288
      webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  @Override
  public boolean onBackPressed() {
    if (webview.canGoBack()) {
      webview.goBack();
      return true;
    } else {
      return false;
    }
  }

  protected abstract void navigateToInitialPage(WebView webview);

  protected void onPageLoaded(WebView webview, String url) {
  }

  protected boolean shouldOverride(WebView webview, String url) {
    return false;
  }

  protected boolean onHandleCallback(WebView webview, String url) {
    return false;
  }
}
