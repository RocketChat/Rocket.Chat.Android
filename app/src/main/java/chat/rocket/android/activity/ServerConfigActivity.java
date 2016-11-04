package chat.rocket.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.server_config.ConnectingToHostFragment;
import chat.rocket.android.fragment.server_config.InputHostnameFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.service.RocketChatService;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;

/**
 * Activity for Login, Sign-up, and Connecting...
 */
public class ServerConfigActivity extends AbstractFragmentActivity {

    @Override
    protected int getLayoutContainerForFragment() {
        return R.id.content;
    }

    private String mServerConfigId;
    private RealmObjectObserver<ServerConfig> mServerConfigObserver =
            new RealmObjectObserver<ServerConfig>() {
                @Override
                protected RealmQuery<ServerConfig> query(Realm realm) {
                    return realm.where(ServerConfig.class).equalTo("id", mServerConfigId);
                }

                @Override
                protected void onChange(ServerConfig config) {
                    onRenderServerConfig(config);
                }
            };

    /**
     * Start the ServerConfigActivity with considering the priority of ServerConfig in the list.
     */
    public static boolean launchFor(Context context, List<ServerConfig> configList) {
        for (ServerConfig config: configList) {
            if (TextUtils.isEmpty(config.getHostname())) {
                return launchFor(context, config);
            } else if (!TextUtils.isEmpty(config.getConnectionError())) {
                return launchFor(context, config);
            }
        }

        for (ServerConfig config: configList) {
            if (config.getAuthProviders().isEmpty()) {
                return launchFor(context, config);
            }
        }

        for (ServerConfig config: configList) {
            if (TextUtils.isEmpty(config.getSelectedProviderName())) {
                return launchFor(context, config);
            }
        }

        for (ServerConfig config: configList) {
            if (TextUtils.isEmpty(config.getToken())) {
                return launchFor(context, config);
            }
        }

        for (ServerConfig config: configList) {
            if (!config.isTokenVerified()) {
                return launchFor(context, config);
            }
        }

        return false;
    }

    private static boolean launchFor(Context context, ServerConfig config) {
        LaunchUtil.showServerConfigActivity(context, config.getId());
        return true;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }

        mServerConfigId = intent.getStringExtra("id");
        if (TextUtils.isEmpty(mServerConfigId)) {
            finish();
            return;
        }

        setContentView(R.layout.simple_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RocketChatService.keepalive(this);
        mServerConfigObserver.sub();
    }

    @Override
    protected void onPause() {
        mServerConfigObserver.unsub();
        super.onPause();
    }

    private void onRenderServerConfig(ServerConfig config) {
        if (config == null) {
            finish();
            return;
        }

        if (config.isTokenVerified()) {
            finish();
            return;
        }

        final String token = config.getToken();
        if (!TextUtils.isEmpty(token)) {
            return;
        }

        final String selectedProviderName = config.getSelectedProviderName();
        if (!TextUtils.isEmpty(selectedProviderName)) {

            return;
        }

        RealmList<MeteorLoginServiceConfiguration> providers = config.getAuthProviders();
        if (!providers.isEmpty()) {

            return;
        }

        final String error = config.getConnectionError();
        String hostname = config.getHostname();
        if (!TextUtils.isEmpty(hostname) && TextUtils.isEmpty(error)) {
            showFragment(new ConnectingToHostFragment());
            return;
        }

        showFragment(new InputHostnameFragment());
    }

    @Override
    protected void showFragment(Fragment f) {
        injectIdArgTo(f);
        super.showFragment(f);
    }

    @Override
    protected void showFragmentWithBackStack(Fragment f) {
        injectIdArgTo(f);
        super.showFragmentWithBackStack(f);
    }

    private void injectIdArgTo(Fragment f) {
        Bundle args = f.getArguments();
        if (args == null) args = new Bundle();
        args.putString("id", mServerConfigId);
        f.setArguments(args);
    }

    @Override
    public void onBackPressed() {
        if (ServerConfig.hasActiveConnection()) {
            super.onBackPressed();
        } else {
            moveTaskToBack(true);
        }
    }
}
