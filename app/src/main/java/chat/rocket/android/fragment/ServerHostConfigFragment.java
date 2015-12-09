package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.ViewUtil;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class ServerHostConfigFragment extends AbstractFragment {
    private View mRootView;

    public static Fragment create(final String host, final String account, final String password) {
        Bundle args = new Bundle();
        args.putString("host", host);
        Fragment f = new ServerHostConfigFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.serverconf_input_host_screen, container, false);

        mRootView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHost(ViewUtil.getText(mRootView, R.id.txt_login_serverhost).toString());
            }
        });

        Bundle args = getArguments();
        if (args!=null) {
            handleInitValue(args.getString("host", ""));
        }

        return mRootView;
    }

    private void handleInitValue(String host) {
        ((TextView) mRootView.findViewById(R.id.txt_login_serverhost)).setText(host);
    }


    private void addHost(final String host){
        if(TextUtils.isEmpty(host)) return;

        RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
            @Override
            public Object process(SQLiteDatabase db) throws Exception {
                ServerConfig.delete(db, "is_primary=1",null);

                ServerConfig s = new ServerConfig();
                s.id = host;
                s.syncstate = SyncState.NOT_SYNCED;
                s.hostname = host;
                s.isPrimary = true;
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
