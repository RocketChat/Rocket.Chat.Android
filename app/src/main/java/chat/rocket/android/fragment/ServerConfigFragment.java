package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import chat.rocket.android.R;
import chat.rocket.android.ViewUtil;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class ServerConfigFragment extends AbstractFragment {

    private View mRootView;

    public ServerConfigFragment(){}

    public static Fragment create(final String host, final String account, final String password) {
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putString("account", account);
        args.putString("password", password);
        Fragment f = new ServerConfigFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.serverconfig_screen, container, false);

        mRootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServerConfig();
            }
        });

        Bundle args = getArguments();
        if (args!=null) {
            handleInitValue(args.getString("host", ""),
                    args.getString("account", ""),
                    args.getString("password", ""));
        }

        return mRootView;
    }

    private void handleInitValue(String host, String account, String password) {
        ((TextView) mRootView.findViewById(R.id.txt_login_serverhost)).setText(host);
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
        final String host = ViewUtil.getText(mRootView, R.id.txt_login_serverhost).toString();
        final String account = ViewUtil.getText(mRootView, R.id.txt_login_account).toString();
        final String password = ViewUtil.getText(mRootView, R.id.txt_login_password).toString();

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
        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new SplashFragment())
                .addToBackStack(null)
                .commit();
    }
}
