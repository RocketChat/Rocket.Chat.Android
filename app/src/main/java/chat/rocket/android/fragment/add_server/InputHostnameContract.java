package chat.rocket.android.fragment.add_server;

import chat.rocket.android.shared.BaseContract;

public interface InputHostnameContract {

  interface View extends BaseContract.View {
    void showLoader();

    void hideLoader();

    void showInvalidServerError();

    void showConnectionError();

    void showHome();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void connectTo(String hostname);
  }

}
