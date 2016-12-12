package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.fragment.server_config.InputHostnameFragment;
import chat.rocket.android.fragment.server_config.WaitingFragment;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import java.util.UUID;

public class AddServerActivity extends AbstractFragmentActivity {

  private String serverConfigId;

  private RealmListObserver<ServerConfig> configuredServersObserver = RealmStore.getDefault()
      .createListObserver(realm -> realm.where(ServerConfig.class).isNotNull("session").findAll())
      .setOnUpdateListener(results -> {
        if (!results.isEmpty()) {
          RocketChatCache.get(this).edit()
              .putString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, serverConfigId)
              .apply();
          finish();
          overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
      });

  private RealmObjectObserver<ServerConfig> targetServerConfigObserver = RealmStore.getDefault()
      .createObjectObserver(realm ->
          realm.where(ServerConfig.class).equalTo("serverConfigId", serverConfigId))
      .setOnUpdateListener(config -> {
        if (config == null || config.getState() == ServerConfig.STATE_CONNECTION_ERROR) {
          showFragment(new InputHostnameFragment());
        } else {
          showFragment(WaitingFragment.create(getString(R.string.add_server_activity_waiting_server)));
        }
      });

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.simple_screen);
    setupServerConfigId();
  }

  private void setupServerConfigId() {
    ServerConfig config = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).isNull("hostname").findFirst());
    if (config != null) {
      serverConfigId = config.getServerConfigId();
      return;
    }

    config = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class)
            .equalTo("state", ServerConfig.STATE_CONNECTION_ERROR).findFirst());
    if (config != null) {
      serverConfigId = config.getServerConfigId();
      return;
    }

    serverConfigId = UUID.randomUUID().toString();
  }

  @Override protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override protected void onResume() {
    super.onResume();
    configuredServersObserver.sub();
    targetServerConfigObserver.sub();
  }

  @Override protected void onPause() {
    configuredServersObserver.unsub();
    targetServerConfigObserver.unsub();
    super.onPause();
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
    args.putString("serverConfigId", serverConfigId);
    fragment.setArguments(args);
  }

  @Override protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
