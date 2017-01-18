package chat.rocket.android.fragment.server_config;

import android.support.design.widget.Snackbar;
import android.widget.TextView;
import org.json.JSONObject;

import chat.rocket.android.BuildConfig;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractServerConfigFragment {
  RealmObjectObserver<ServerConfig> serverConfigObserver = RealmStore.getDefault()
      .createObjectObserver(realm ->
          realm.where(ServerConfig.class).equalTo(ServerConfig.ID, serverConfigId))
      .setOnUpdateListener(this::onRenderServerConfig);

  Subscription serverPolicySubscription;

  public InputHostnameFragment() {
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_input_hostname;
  }

  @Override
  protected void onSetupView() {
    setupVersionInfo();

    rootView.findViewById(R.id.btn_connect).setOnClickListener(view -> handleConnect());

    serverConfigObserver.sub();
  }

  private void setupVersionInfo() {
    TextView versionInfoView = (TextView) rootView.findViewById(R.id.version_info);
    versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
  }

  private void handleConnect() {
    final String hostname = ServerPolicyHelper.enforceHostname(getHostname());

    final ServerPolicyApi serverPolicyApi =
        new DefaultServerPolicyApi(OkHttpHelper.getClientForUploadFile(), hostname);

    final ServerPolicyApiValidationHelper validationHelper =
        new ServerPolicyApiValidationHelper(serverPolicyApi);

    if (serverPolicySubscription != null) {
      serverPolicySubscription.unsubscribe();
    }

    serverPolicySubscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            serverValidation -> {
              if (serverValidation.isValid()) {
                onServerValid(hostname, serverValidation.usesSecureConnection());
              } else {
                showError(getString(R.string.input_hostname_invalid_server_message));
              }
            },
            throwable -> {
              showError(getString(R.string.connection_error_try_later));
            });
  }

  @Override
  public void onDestroyView() {
    serverConfigObserver.unsub();
    if (serverPolicySubscription != null) {
      serverPolicySubscription.unsubscribe();
    }
    super.onDestroyView();
  }

  private String getHostname() {
    final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

    return TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString();
  }

  private void onServerValid(final String hostname, boolean usesSecureConnection) {
    RocketChatCache.get(getContext()).edit()
        .putString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, serverConfigId)
        .apply();

    RealmStore.getDefault().executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class,
            new JSONObject().put(ServerConfig.ID, serverConfigId)
                .put(ServerConfig.HOSTNAME, hostname)
                .put(ServerConfig.ERROR, JSONObject.NULL)
                .put(ServerConfig.SESSION, JSONObject.NULL)
                .put(ServerConfig.SECURE_CONNECTION, usesSecureConnection)
                .put(ServerConfig.STATE, ServerConfig.STATE_READY)))
        .continueWith(new LogcatIfError());
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
