package chat.rocket.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.login.GitHubOAuthWebViewFragment;
import chat.rocket.android.fragment.server_config.AuthenticatingFragment;
import chat.rocket.android.fragment.server_config.ConnectingToHostFragment;
import chat.rocket.android.fragment.server_config.InputHostnameFragment;
import chat.rocket.android.fragment.server_config.LoginFragment;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ServerConfigCredential;
import chat.rocket.android.service.RocketChatService;
import io.realm.Realm;
import io.realm.RealmQuery;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 * Activity for Login, Sign-up, and Connecting...
 */
public class ServerConfigActivity extends AbstractFragmentActivity {

  private String serverConfigId;
  private RealmObjectObserver<ServerConfig> serverConfigObserver =
      new RealmObjectObserver<ServerConfig>() {
        @Override protected RealmQuery<ServerConfig> query(Realm realm) {
          return realm.where(ServerConfig.class).equalTo("id", serverConfigId);
        }

        @Override protected void onChange(ServerConfig config) {
          onRenderServerConfig(config);
        }
      };

  /**
   * Start the ServerConfigActivity with considering the priority of ServerConfig in the list.
   */
  public static boolean launchFor(Context context, List<ServerConfig> configList) {
    for (ServerConfig config : configList) {
      if (TextUtils.isEmpty(config.getHostname())) {
        return launchFor(context, config);
      } else if (!TextUtils.isEmpty(config.getConnectionError())) {
        return launchFor(context, config);
      }
    }

    for (ServerConfig config : configList) {
      if (TextUtils.isEmpty(config.getSession())) {
        return launchFor(context, config);
      }
    }

    for (ServerConfig config : configList) {
      ServerConfigCredential credential = config.getCredential();
      if (credential != null && !TextUtils.isEmpty(credential.getType())) {
        return launchFor(context, config);
      }
    }

    for (ServerConfig config : configList) {
      if (TextUtils.isEmpty(config.getToken())) {
        return launchFor(context, config);
      }
    }

    for (ServerConfig config : configList) {
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

  @Override protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (intent == null || intent.getExtras() == null) {
      finish();
      return;
    }

    serverConfigId = intent.getStringExtra("id");
    if (TextUtils.isEmpty(serverConfigId)) {
      finish();
      return;
    }

    setContentView(R.layout.simple_screen);
  }

  @Override protected void onResume() {
    super.onResume();
    RocketChatService.keepalive(this);
    serverConfigObserver.sub();
  }

  @Override protected void onPause() {
    serverConfigObserver.unsub();
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

    final ServerConfigCredential credential = config.getCredential();
    if (credential != null && !TextUtils.isEmpty(credential.getType())) {
      if (ServerConfigCredential.hasSecret(credential)) {
        showFragment(new AuthenticatingFragment());
      } else {
        showFragment(getAuthFragmentFor(credential.getType()));
      }
      return;
    }

    if (!TextUtils.isEmpty(config.getSession())) {
      showFragment(new LoginFragment());
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

  private Fragment getAuthFragmentFor(final String authType) {
    if ("github".equals(authType)) {
      return GitHubOAuthWebViewFragment.create(serverConfigId);
    } else if ("twitter".equals(authType)) {
      // TODO
    }

    RealmHelperBolts.executeTransaction(realm -> realm.where(ServerConfigCredential.class)
        .equalTo("type", authType)
        .findAll()
        .deleteAllFromRealm()
    ).continueWith(new LogcatIfError());
    throw new IllegalArgumentException("Invalid authType given:" + authType);
  }

  @Override protected void showFragment(Fragment fragment) {
    injectServerConfigIdArgTo(fragment);
    super.showFragment(fragment);
  }

  @Override protected void showFragmentWithBackStack(Fragment fragment) {
    injectServerConfigIdArgTo(fragment);
    super.showFragmentWithBackStack(fragment);
  }

  private void injectServerConfigIdArgTo(Fragment fragment) {
    Bundle args = fragment.getArguments();
    if (args == null) {
      args = new Bundle();
    }
    args.putString("id", serverConfigId);
    fragment.setArguments(args);
  }

  @Override public void onBackPressed() {
    if (ServerConfig.hasActiveConnection()) {
      super.onBackPressed();
    } else {
      moveTaskToBack(true);
    }
  }
}
