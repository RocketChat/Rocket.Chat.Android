package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import java.util.List;
import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomList(@NonNull List<Room> roomList);

    void showUserStatus(@NonNull User user);

    void show(User user);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(Room room);

    void onSpotlightSelected(Spotlight spotlight);

    Flowable<List<Spotlight>> searchSpotlight(String term);

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout();
  }
}
