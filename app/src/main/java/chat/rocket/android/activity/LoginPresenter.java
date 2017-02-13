package chat.rocket.android.activity;

import android.support.annotation.NonNull;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.SessionInteractor;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class LoginPresenter extends BasePresenter<LoginContract.View>
    implements LoginContract.Presenter {

  private final String hostname;
  private final SessionInteractor sessionInteractor;
  private final ConnectivityManagerApi connectivityManagerApi;

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
      view.close();
      return;
    }

    loadSessionState();
  }

  private void loadSessionState() {
    final Subscription subscription = sessionInteractor.getSessionState()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(state -> {
          switch (state) {
            case UNAVAILABLE:
              view.showLogin(hostname);
              break;
            case INVALID:
              view.showRetryLogin(hostname);
              break;
            case VALID:
              view.close();
          }
        });

    addSubscription(subscription);
  }
}
