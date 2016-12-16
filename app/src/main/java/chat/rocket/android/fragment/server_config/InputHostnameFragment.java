package chat.rocket.android.fragment.server_config;

import android.support.design.widget.Snackbar;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import org.json.JSONObject;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractServerConfigFragment {
  RealmObjectObserver<ServerConfig> serverConfigObserver = RealmStore.getDefault()
      .createObjectObserver(realm ->
          realm.where(ServerConfig.class).equalTo("serverConfigId", serverConfigId))
      .setOnUpdateListener(this::onRenderServerConfig);

  public InputHostnameFragment() {
  }

  @Override protected int getLayout() {
    return R.layout.fragment_input_hostname;
  }

  @Override protected void onSetupView() {
    rootView.findViewById(R.id.btn_connect).setOnClickListener(view -> handleConnect());

    serverConfigObserver.sub();
  }

  private void handleConnect() {
    final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

    final String hostname =
        TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString();

    RocketChatCache.get(getContext()).edit()
        .putString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, serverConfigId)
        .apply();

    RealmStore.getDefault().executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class,
            new JSONObject().put("serverConfigId", serverConfigId)
                .put("hostname", hostname)
                .put("error", JSONObject.NULL)
                .put("session", JSONObject.NULL)
                .put("state", ServerConfig.STATE_READY))).continueWith(new LogcatIfError());
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onDestroyView() {
    serverConfigObserver.unsub();
    super.onDestroyView();
  }

  private void showError(String errString) {
    Snackbar.make(rootView, errString, Snackbar.LENGTH_LONG).show();
  }

  private void onRenderServerConfig(ServerConfig config) {
    if (config == null) {
      return;
    }

    final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

    if (!TextUtils.isEmpty(config.getHostname())) {
      editor.setText(config.getHostname());
    }
    if (!TextUtils.isEmpty(config.getError())) {
      showError(config.getError());
    }
  }
}
