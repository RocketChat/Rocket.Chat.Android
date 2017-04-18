package chat.rocket.android.fragment.server_config;

import chat.rocket.android.shared.BaseContract;

public interface TwoStepAuthContract {

  interface View extends BaseContract.View {

    void showLoading();

    void hideLoading();

    void showError(String message);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onCode(String twoStepAuthCode);
  }
}
