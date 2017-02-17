package chat.rocket.android.activity;

import chat.rocket.android.shared.BaseContract;

public interface MainContract {

  interface View extends BaseContract.View {

    void showHome();

    void showRoom(String hostname, String roomId);

    void showUnreadCount(long roomsCount, int mentionsCount);

    void showLoginScreen();

    void showConnectionError();

    void showConnecting();

    void showConnectionOk();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onOpenRoom(String hostname, String roomId);

    void onRetryLogin();
  }
}
