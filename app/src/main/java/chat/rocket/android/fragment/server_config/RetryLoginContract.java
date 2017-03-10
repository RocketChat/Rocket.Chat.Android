package chat.rocket.android.fragment.server_config;

import chat.rocket.android.shared.BaseContract;

public interface RetryLoginContract {

  interface View extends BaseContract.View {

    void showRetry(String token);

    void showError(String message);

    void showLoader();

    void hideLoader();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onLogin(String token);
  }
}
