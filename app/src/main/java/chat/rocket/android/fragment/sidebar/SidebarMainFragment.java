package chat.rocket.android.fragment.sidebar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.RoomListManager;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;

public class SidebarMainFragment extends AbstractFragment {

  private String serverConfigId;
  private RoomListManager roomListManager;
  private RealmListObserver<RoomSubscription> roomsObserver;

  public SidebarMainFragment() {
  }

  public static SidebarMainFragment create(String serverConfigId) {
    Bundle args = new Bundle();
    args.putString("serverConfigId", serverConfigId);

    SidebarMainFragment fragment = new SidebarMainFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    serverConfigId = args == null ? null : args.getString("serverConfigId");
    if (!TextUtils.isEmpty(serverConfigId)) {
      RealmHelper realmHelper = RealmStore.get(serverConfigId);
      if (realmHelper != null) {
        roomsObserver = realmHelper
            .createListObserver(realm -> realm.where(RoomSubscription.class).findAll())
            .setOnUpdateListener(list -> roomListManager.setRooms(list));
      }
    }
  }

  @Override protected int getLayout() {
    if (serverConfigId == null) {
      return R.layout.simple_screen;
    } else {
      return R.layout.fragment_sidebar_main;
    }
  }

  @Override protected void onSetupView() {
    if (serverConfigId == null) {
      return;
    }

    setupUserActionToggle();

    roomListManager = new RoomListManager(
        (LinearLayout) rootView.findViewById(R.id.channels_container),
        (LinearLayout) rootView.findViewById(R.id.direct_messages_container));
    roomListManager.setOnItemClickListener(view -> {
      RocketChatCache.get(view.getContext()).edit()
          .putString(RocketChatCache.KEY_SELECTED_ROOM_ID, view.getRoomId())
          .apply();
    });
  }

  private void setupUserActionToggle() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) rootView.findViewById(R.id.toggle_user_action));
    toggleUserAction.setFocusableInTouchMode(false);
    rootView.findViewById(R.id.user_info_container).setOnClickListener(view -> {
      toggleUserAction.toggle();
    });
    RxCompoundButton.checkedChanges(toggleUserAction)
        .compose(bindToLifecycle())
        .subscribe(RxView.visibility(rootView.findViewById(R.id.user_action_outer_container)));
  }

  @Override public void onResume() {
    super.onResume();
    if (roomsObserver != null) {
      roomsObserver.sub();
    }
  }

  @Override public void onPause() {
    if (roomsObserver != null) {
      roomsObserver.unsub();
    }
    super.onPause();
  }
}
