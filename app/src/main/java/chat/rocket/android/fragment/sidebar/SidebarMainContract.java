package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.core.User;

public interface SidebarMainContract {

  interface View {

    void showScreen();

    void showEmptyScreen();

    void showRoomList(List<Room> roomList);

    void showUser(User user);
  }

  interface Presenter {
    void bindView(@NonNull View view);

    void release();

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout();
  }
}
