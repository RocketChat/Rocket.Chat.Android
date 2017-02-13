package chat.rocket.android.fragment.add_server;

import android.content.SharedPreferences;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InputHostnamePresenter extends BasePresenter<InputHostnameContract.View>
    implements InputHostnameContract.Presenter {

  private final SharedPreferences rocketChatCache;
  private final ConnectivityManagerApi connectivityManager;

  public InputHostnamePresenter(SharedPreferences rocketChatCache,
                                ConnectivityManagerApi connectivityManager) {
    this.rocketChatCache = rocketChatCache;
    this.connectivityManager = connectivityManager;
  }

  @Override
  public void connectTo(final String hostname) {
    view.showLoader();

    connectToEnforced(ServerPolicyHelper.enforceHostname(hostname));
  }

  public void connectToEnforced(final String hostname) {
    final ServerPolicyApi serverPolicyApi =
        new DefaultServerPolicyApi(OkHttpHelper.getClientForUploadFile(), hostname);

    final ServerPolicyApiValidationHelper validationHelper =
        new ServerPolicyApiValidationHelper(serverPolicyApi);

    clearSubscripions();

    final Subscription subscription = ServerPolicyHelper.isApiVersionValid(validationHelper)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(() -> view.hideLoader())
        .subscribe(
            serverValidation -> {
              if (serverValidation.isValid()) {
                onServerValid(hostname, serverValidation.usesSecureConnection());
              } else {
                view.showInvalidServerError();
              }
            },
            throwable -> {
              view.showConnectionError();
            });

    addSubscription(subscription);
  }

  private void onServerValid(final String hostname, boolean usesSecureConnection) {
    rocketChatCache.edit()
        .putString(RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME, hostname)
        .apply();

    connectivityManager.addOrUpdateServer(hostname, hostname, !usesSecureConnection);
    connectivityManager.keepAliveServer();

    view.showHome();
  }
}
