package chat.rocket.android.fragment.add_server;

import android.support.annotation.NonNull;

public interface InputHostnameContract {

  interface View {
    void showLoader();

    void hideLoader();

    void showInvalidServerError();

    void showConnectionError();

    void showHome();
  }

  interface Presenter {

    void bindView(@NonNull View view);

    void release();

    void connectTo(String hostname);
  }

}
