package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import chat.rocket.android.R;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class AuthenticateFragment extends AbstractFragment {
    public AuthenticateFragment(){}

    private ConstrainedActionManager mSaveAuthManager = new ConstrainedActionManager() {
        @Override
        protected void action() {
            if(mServerConfig != null) {
                RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                    @Override
                    public Object process(SQLiteDatabase db) {
                        mServerConfig.put(db);
                        return null;
                    }
                });
                showSplashFragment();
            }
        }
    };
    private ServerConfig mServerConfig;

    public static Fragment create(final String host, final String account, final String password) {
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putString("account", account);
        args.putString("password", password);
        Fragment f = new AuthenticateFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(!isValidArgs(args)){
            finish();
            return;
        }

        handleLogin(args.getString("host"),
                args.getString("account"),
                args.getString("password"));
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

    private void handleLogin(final String host, final String account, final String password){
        RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
            @Override
            public Object process(SQLiteDatabase db) throws Exception {
                ServerConfig s = new ServerConfig();
                s.id = host;
                s.syncstate = SyncState.NOT_SYNCED;
                s.hostname = host;
                s.account = account;
                s.passwd = sha256sum(password);
                s.isPrimary = true;
                s.authToken = "";
                s.put(db);

                return null;
            }
        });

        RocketChatWSService.keepalive(getContext());
    }

    private void showSplashFragment(){
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, new SplashFragment())
                .commit();
    }

    private void showErrorFragment(String msg) {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, LoginErrorFragment.create(msg))
                .commit();
    }

    private boolean isValidArgs(Bundle args) {
        if(args == null) return false;

        return (args.containsKey("host") &&
                args.containsKey("account") &&
                args.containsKey("password"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authenticating_screen, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSaveAuthManager.setConstrainedMet(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSaveAuthManager.setConstrainedMet(false);
    }
}
