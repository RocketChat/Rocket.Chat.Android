package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.squareup.okhttp.HttpUrl;

import org.json.JSONObject;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class GitHubOAuthWebViewFragment extends AbstractWebViewFragment {

    private String mHost;
    private String mUrl;
    private boolean mResultOK;

    public static Fragment create(final String host, final String clientId) {
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putString("client_id", clientId);
        Fragment f = new GitHubOAuthWebViewFragment();
        f.setArguments(args);
        return f;
    }

    private boolean hasValidArgs(Bundle args) {
        if(args == null) return false;
        return args.containsKey("host")
                && args.containsKey("client_id");
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(!hasValidArgs(args)) {
            throw new IllegalArgumentException("Params 'roomId' and 'roomName' are required for creating ChatRoomFragment");
        }

        mHost = args.getString("host");
        mUrl = generateURL(args.getString("client_id"));
    }

    private String generateURL(String clientId){
        try {
            String state = Base64.encodeToString(new JSONObject()
                    .put("loginStyle", "popup")
                    .put("credentialToken", "github" + System.currentTimeMillis())
                    .put("isCordova", true).toString().getBytes(), Base64.NO_WRAP);

            return new HttpUrl.Builder()
                    .scheme("https")
                    .host("github.com")
                    .addPathSegment("login")
                    .addPathSegment("oauth")
                    .addPathSegment("authorize")
                    .addQueryParameter("client_id", clientId)
                    .addQueryParameter("scope", "user:email")
                    .addQueryParameter("state", state)
                    .build().toString();
        }
        catch (Exception e){
        }
        return null;
    }

    @Override
    protected void navigateToInitialPage(WebView webview) {
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }

        mResultOK = false;
        webview.loadUrl(mUrl);
        webview.addJavascriptInterface(new JSInterface(new JSInterfaceCallback() {
            @Override
            public void hanldeResult(@Nullable JSONObject result) {
                // onPageFinish is called twice... Should ignore latter one.
                if(mResultOK) return;

                if (result!=null && result.optBoolean("setCredentialToken",false)) {
                    try {
                        final String credentialToken = result.getString("credentialToken");
                        final String credentialSecret = result.getString("credentialSecret");
                        ServerConfig conf = RocketChatDatabaseHelper.read(getContext(), new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
                            @Override
                            public ServerConfig process(SQLiteDatabase db) throws Exception {
                                return ServerConfig.getPrimaryConfig(db);
                            }
                        });

                        if(conf!=null) {
                            conf.authType = ServerConfig.AuthType.GITHUB;
                            conf.password = new JSONObject()
                                    .put("credentialToken",credentialToken)
                                    .put("credentialSecret",credentialSecret)
                                    .toString();
                            conf.syncstate = SyncState.NOT_SYNCED;
                            conf.putByContentProvider(getContext());
                            mResultOK = true;
                        }
                    }
                    catch (Exception e) {
                        Log.e(Constants.LOG_TAG,"error",e);
                    }
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.simple_framelayout, new SplashFragment())
                        .commit();
            }
        }),"_rocketchet_hook");
    }

    @Override
    protected void onPageLoaded(WebView webview, String url) {
        super.onPageLoaded(webview, url);

        if(url.contains(mHost) && url.contains("_oauth/github?close")) {
            webview.loadUrl("javascript:window._rocketchet_hook.handleConfig(document.getElementById('config').innerText);");
        }
    }

    private static interface JSInterfaceCallback {
        void hanldeResult(@Nullable JSONObject result);
    }
    private static final class JSInterface {
        private final JSInterfaceCallback mCallback;

        public JSInterface(JSInterfaceCallback callback) {
            mCallback = callback;
        }

        @JavascriptInterface
        public void handleConfig(String config) {
            try {
                mCallback.hanldeResult(new JSONObject(config));
            }
            catch (Exception e){
                mCallback.hanldeResult(null);
            }
        }
    }
}
