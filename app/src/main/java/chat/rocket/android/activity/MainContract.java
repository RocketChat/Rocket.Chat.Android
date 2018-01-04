package chat.rocket.android.activity;

import java.util.List;

import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.utils.Pair;

public interface MainContract {

  interface View extends BaseContract.View {

    void showHome();

    void showRoom(String hostname, String roomId);

    void showUnreadCount(long roomsCount, int mentionsCount);

    void showAddServerScreen();

    void showLoginScreen();

    void showConnectionError();

    void showConnecting();

    void showConnectionOk();

    void showSignedInServers(List<Pair<String, Pair<String, String>>> serverList);

    void refreshRoom();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onOpenRoom(String hostname, String roomId);

    void onRetryLogin();

    void bindViewOnly(View view);

    void loadSignedInServers(String hostname);

    void prepareToLogout();
  }
}
