package chat.rocket.android.fragment;

import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.ViewUtil;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class ServerAuthConfigFragment extends AbstractFragment {

    private View mRootView;

    public ServerAuthConfigFragment(){}

    public static Fragment create(final String account, final String password) {
        Bundle args = new Bundle();
        args.putString("account", account);
        args.putString("password", password);
        Fragment f = new ServerAuthConfigFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.serverconf_input_auth_screen, container, false);

        mRootView.findViewById(R.id.btn_login_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServerConfig();
            }
        });

        Bundle args = getArguments();
        if (args!=null) {
            handleInitValue(args.getString("account", ""),
                    args.getString("password", ""));
        }

        return mRootView;
    }

    private void setButtonEnabled(@IdRes int btn, boolean enabled) {
        if (btn==R.id.btn_login_github) {
            mRootView.findViewById(btn).setEnabled(enabled);
        }
        else {
            mRootView.findViewById(btn).setVisibility(enabled? View.VISIBLE : View.GONE);
        }
    }

    private void handleInitValue(String account, String password) {
        ((TextView) mRootView.findViewById(R.id.txt_login_account)).setText(account);
        ((TextView) mRootView.findViewById(R.id.txt_login_password)).setText(password);
    }

    private static String sha256sum(String orig) {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        d.update(orig.getBytes());

        StringBuilder sb = new StringBuilder();
        for(byte b : d.digest()) sb.append(String.format("%02x", b & 0xff));

        return sb.toString();
    }


    private void addServerConfig(){
        final String account = ViewUtil.getText(mRootView, R.id.txt_login_account).toString();
        final String password = ViewUtil.getText(mRootView, R.id.txt_login_password).toString();

        if(TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) return;


        ServerConfig s = RocketChatDatabaseHelper.read(getContext(), new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) throws Exception {
                return ServerConfig.getPrimaryConfig(db);
            }
        });
        s.syncstate = SyncState.NOT_SYNCED;
        s.account = account;
        s.authType = ServerConfig.AuthType.EMAIL;
        s.password = sha256sum(password);
        s.isPrimary = true;
        s.authToken = "";
        s.putByContentProvider(getContext());

        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new SplashFragment())
                .addToBackStack(null)
                .commit();
    }

    private ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            if(getContext()==null) return;
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    setupOAuthProviders();
                }
            });
        }
    };

    private void setupOAuthProviders() {
        ServerConfig conf = getPrimaryServerConfig();

        if (conf == null) {//deleted!
            finish();
            return;
        }

        JSONObject providers = conf.getOAuthProviders();
        setButtonEnabled(R.id.btn_login_github,!providers.isNull("github"));
        setButtonEnabled(R.id.btn_login_twitter,!providers.isNull("twitter"));

        try {
            if (!providers.isNull("github")) {
                final String host = conf.hostname;
                final String clientId = providers.getJSONObject("github").getString("client_id");
                mRootView.findViewById(R.id.btn_login_github).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.simple_framelayout, GitHubOAuthWebViewFragment.create(host, clientId))
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
            if (!providers.isNull("twitter")) {
                final String host = conf.hostname;
                final String consumerKey = providers.getJSONObject("twitter").getString("consumer_key");
                mRootView.findViewById(R.id.btn_login_twitter).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(Constants.LOG_TAG,"Not Implemented: [consumerKey="+consumerKey+"]");
                        Toast.makeText(v.getContext(), "Not Implemented: [consumerKey="+consumerKey+"]",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        catch (Exception e) {
            Log.e(Constants.LOG_TAG,"error",e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupOAuthProviders();
        getContext().getContentResolver().registerContentObserver(RocketChatProvider.getUriForQuery(ServerConfig.TABLE_NAME), true, mObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onDestroy() {
        ServerConfig conf = getPrimaryServerConfig();
        if(conf==null || conf.authType== ServerConfig.AuthType.UNSPECIFIED) {
            RocketChatWSService.kill(getContext());
        }

        super.onDestroy();
    }
}
