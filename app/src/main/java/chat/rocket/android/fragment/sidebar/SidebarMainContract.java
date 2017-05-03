package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.SpotlightRoom;
import chat.rocket.core.models.User;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomList(@NonNull List<Room> roomList);

    void show(User user, RocketChatAbsoluteUrl absoluteUrl);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(Room room);

    void onSpotlightRoomSelected(SpotlightRoom spotlightRoom);

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout();
  }
}
