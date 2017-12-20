package chat.rocket.android.fragment.add_server;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class InputHostnamePresenter extends BasePresenter<InputHostnameContract.View> implements InputHostnameContract.Presenter {
  private final ConnectivityManagerApi connectivityManager;
  private boolean isValidServerUrl;

  public InputHostnamePresenter(ConnectivityManagerApi connectivityManager) {
    this.connectivityManager = connectivityManager;
  }

  @Override
  public void connectTo(final String hostname) {
    view.showLoader();
    connectToEnforced(ServerPolicyHelper.enforceHostname(hostname));
  }

  public void connectToEnforced(final String hostname) {
    final ServerPolicyApi serverPolicyApi = new DefaultServerPolicyApi(OkHttpHelper.INSTANCE.getClientForUploadFile(), hostname);
    final ServerPolicyApiValidationHelper validationHelper = new ServerPolicyApiValidationHelper(serverPolicyApi);

    clearSubscriptions();

    final Disposable subscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> view.hideLoader(isValidServerUrl))
        .subscribe(
            serverValidation -> {
              if (serverValidation.isValid()) {
                isValidServerUrl=true;
                onServerValid(hostname, serverValidation.usesSecureConnection());
              } else {
                view.showInvalidServerError();
              }
            },
            throwable -> {
              Logger.INSTANCE.report(throwable);
              view.showConnectionError();
            });
    addSubscription(subscription);
  }

  private void onServerValid(String hostname, boolean usesSecureConnection) {
    RocketChatCache.INSTANCE.setSelectedServerHostname(hostname);

    String server = hostname.replace("/", ".");
    connectivityManager.addOrUpdateServer(server, server, !usesSecureConnection);
    connectivityManager.keepAliveServer();

    view.showHome();
  }
}