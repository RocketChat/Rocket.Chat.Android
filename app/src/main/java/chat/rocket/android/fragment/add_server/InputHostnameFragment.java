package chat.rocket.android.fragment.add_server;

import android.support.design.widget.Snackbar;
import android.widget.TextView;

import chat.rocket.android.BuildConfig;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.ConnectivityManagerApi;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractFragment {
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

    rootView.findViewById(R.id.btn_connect).setEnabled(false);

    serverPolicySubscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> rootView.findViewById(R.id.btn_connect).setEnabled(true))
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
        .putString(RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME, hostname)
        .apply();

    ConnectivityManagerApi connectivityManager =
        ConnectivityManager.getInstance(getContext().getApplicationContext());
    connectivityManager.addOrUpdateServer(hostname, hostname, !usesSecureConnection);
    connectivityManager.keepAliveServer();

    LaunchUtil.showMainActivity(getContext());
    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  private void showError(String errString) {
    Snackbar.make(rootView, errString, Snackbar.LENGTH_LONG).show();
  }
}
