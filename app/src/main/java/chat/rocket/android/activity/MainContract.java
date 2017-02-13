package chat.rocket.android.activity;

import chat.rocket.android.shared.BaseContract;

public interface MainContract {

  interface View extends BaseContract.View {

    void showHome();

    void showRoom(String hostname, String roomId);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onOpenRoom(String hostname, String roomId);
  }
}
