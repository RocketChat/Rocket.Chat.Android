package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;

/**
 * Login screen.
 */
public class RetryConnectFragment extends AbstractServerConfigFragment {
  private RealmObjectObserver<ServerConfig> serverConfigObserver;

  @Override
  protected int getLayout() {
    return R.layout.fragment_retry_login;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    serverConfigObserver = RealmStore.getDefault()
        .createObjectObserver(realm ->
            realm.where(ServerConfig.class).equalTo(ServerConfig.ID, serverConfigId))
        .setOnUpdateListener(this::onRenderServerConfig);
  }

  @Override
  protected void onSetupView() {
    rootView.findViewById(R.id.waiting).setVisibility(View.GONE);

    final View btnRetry = rootView.findViewById(R.id.btn_retry_login);
    btnRetry.setOnClickListener(view -> {
      RealmStore.getDefault()
          .executeTransaction(realm -> {
            ServerConfig config = realm.where(ServerConfig.class)
                .equalTo(ServerConfig.ID, serverConfigId).findFirst();
            if (config != null && config.getState() == ServerConfig.STATE_CONNECTION_ERROR) {
              config.setState(ServerConfig.STATE_READY);
            }
            return null;
          }).continueWith(new LogcatIfError());
    });
  }

  private void onRenderServerConfig(ServerConfig config) {
    if (config == null) {
      return;
    }

    final String error = config.getError();
    final TextView txtError = (TextView) rootView.findViewById(R.id.txt_error_description);
    if (!TextUtils.isEmpty(error)) {
      txtError.setText(error);
    }

    final int state = config.getState();
    if (state == ServerConfig.STATE_CONNECTED) {
      finish();
    }
    rootView.findViewById(R.id.btn_retry_login)
        .setEnabled(state == ServerConfig.STATE_CONNECTION_ERROR);
  }

  @Override
  public void onResume() {
    super.onResume();
    serverConfigObserver.sub();
  }

  @Override
  public void onPause() {
    serverConfigObserver.unsub();
    super.onPause();
  }
}
