package chat.rocket.android.activity;

import chat.rocket.android.shared.BaseContract;

public interface LoginContract {

  interface View extends BaseContract.View {
    void showLogin(String hostname);

    void showRetryLogin(String hostname);

    void closeView();
  }

  interface Presenter extends BaseContract.Presenter<View> {
  }
}
