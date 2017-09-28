package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;

import bolts.Continuation;
import chat.rocket.core.models.RoomSidebar;
import io.reactivex.Flowable;
import java.util.List;
import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList);

    void filterRoomSidebarList(CharSequence term);

    void show(User user);

    void onLogoutCleanUp();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(RoomSidebar roomSidebar);

    void onSpotlightSelected(Spotlight spotlight);

    Flowable<List<Spotlight>> searchSpotlight(String term);

    void disposeSubscriptions();

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout(Continuation<Void, Object> continuation);

    void beforeLogoutCleanUp();
  }
}