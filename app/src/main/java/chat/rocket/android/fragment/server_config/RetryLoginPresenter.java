package chat.rocket.android.fragment.server_config;

import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Session;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RetryLoginPresenter extends BasePresenter<RetryLoginContract.View>
    implements RetryLoginContract.Presenter {

  private final SessionInteractor sessionInteractor;
  private final MethodCallHelper methodCallHelper;

  public RetryLoginPresenter(SessionInteractor sessionInteractor,
                             MethodCallHelper methodCallHelper) {
    this.sessionInteractor = sessionInteractor;
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void bindView(@NonNull RetryLoginContract.View view) {
    super.bindView(view);

    subscribeToDefaultSession();
  }

  @Override
  public void onLogin(String token) {
    view.showLoader();

    methodCallHelper.loginWithToken(token)
        .continueWith(task -> {
          if (task.isFaulted()) {
            view.hideLoader();
          }
          return null;
        });
  }

  private void subscribeToDefaultSession() {
    addSubscription(
        sessionInteractor.getDefault()
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::onSession,
                Logger::report
            )
    );
  }

  private void onSession(Optional<Session> sessionOptional) {
    if (!sessionOptional.isPresent()) {
      return;
    }

    final Session session = sessionOptional.get();

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      view.showRetry(token);
    }

    final String errorMessage = session.getError();
    if (!TextUtils.isEmpty(errorMessage)) {
      view.showError(errorMessage);
    }
  }
}
