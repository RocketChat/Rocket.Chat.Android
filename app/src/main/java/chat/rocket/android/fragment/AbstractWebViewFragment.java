package chat.rocket.android.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import chat.rocket.android.R;
import chat.rocket.android.activity.OnBackPressListener;
import hugo.weaving.DebugLog;

abstract class AbstractWebViewFragment extends Fragment implements OnBackPressListener {
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup mRootView = (ViewGroup)inflater.inflate(R.layout.simple_webview, container, false);
        mWebView = (WebView) mRootView.findViewById(R.id.simple_webview);
        setupWebView();

        navigateToInitialPage(mWebView);
        return mRootView;
    }

    private void setupWebView() {
        WebSettings settings = mWebView.getSettings();
        if (settings != null) {
            settings.setJavaScriptEnabled(true);
        }
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(mWebViewClient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            //refs: https://code.google.com/p/android/issues/detail?id=35288
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        private boolean mError;
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon){
            mError = false;
        }

        @Override
        public void onPageFinished(WebView webview, String url){
            if(!mError) onPageLoaded(webview, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mError = true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url){
            return (shouldOverride(webview,url) && onHandleCallback(webview, url)) || super.shouldOverrideUrlLoading(webview, url);
        }

        @DebugLog
        @Override
        public void onFormResubmission (WebView view, Message dontResend, Message resend){
            //resend POST request without confirmation.
            resend.sendToTarget();
        }
    };

    protected void finish(){
        if(getFragmentManager().getBackStackEntryCount()==0){
            getActivity().finish();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onBackPressed()
    {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        else return false;
    }

    protected abstract void navigateToInitialPage(WebView webview);
    protected void onPageLoaded(WebView webview, String url){}

    protected boolean shouldOverride(WebView webview, String url){
        return false;
    }
    protected boolean onHandleCallback(WebView webview, String url){
        return false;
    };
}
