package chat.rocket.android.activity;

import android.support.annotation.NonNull;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.SessionInteractor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class LoginPresenter extends BasePresenter<LoginContract.View>
    implements LoginContract.Presenter {

  private final String hostname;
  private final SessionInteractor sessionInteractor;
  private final ConnectivityManagerApi connectivityManagerApi;

  private boolean isLogging = false;

  public LoginPresenter(String hostname,
                        SessionInteractor sessionInteractor,
                        ConnectivityManagerApi connectivityManagerApi) {
    this.hostname = hostname;
    this.sessionInteractor = sessionInteractor;
    this.connectivityManagerApi = connectivityManagerApi;
  }

  @Override
  public void bindView(@NonNull LoginContract.View view) {
    super.bindView(view);

    connectivityManagerApi.keepAliveServer();

    if (hostname == null || hostname.length() == 0) {
      view.closeView();
      return;
    }

    if (isLogging) {
      return;
    }

    loadSessionState();
  }

  private void loadSessionState() {
    final Disposable subscription = sessionInteractor.getSessionState()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            state -> {
              switch (state) {
                case UNAVAILABLE:
                  isLogging = true;
                  view.showLogin(hostname);
                  break;
                case INVALID:
                  isLogging = false;
                  view.showRetryLogin(hostname);
                  break;
                case VALID:
                  isLogging = false;
                  view.closeView();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }
}
