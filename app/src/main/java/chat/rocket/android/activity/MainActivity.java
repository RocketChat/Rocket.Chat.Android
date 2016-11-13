package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import chat.rocket.android.R;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.helper.Avatar;
import chat.rocket.android.layouthelper.chatroom.RoomListManager;
import chat.rocket.android.model.ddp.RoomSubscription;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {
  private RoomListManager roomListManager;

  @Override protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      showFragment(new HomeFragment());
    }

    setupSidebar();
  }

  private void setupSidebar() {
    setupUserActionToggle();

    roomListManager = new RoomListManager(
        (LinearLayout) findViewById(R.id.channels_container),
        (LinearLayout) findViewById(R.id.direct_messages_container));
    roomListManager.setOnItemClickListener(view -> {
      showRoomFragment(view.getRoomId());
      closeSidebarIfNeeded();
    });

    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null) {
      final SlidingPaneLayout subPane = (SlidingPaneLayout) findViewById(R.id.sub_sliding_pane);
      pane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
        @Override public void onPanelClosed(View panel) {
          super.onPanelClosed(panel);
          if (subPane != null) {
            subPane.closePane();
          }
        }
      });
    }
    ImageView myAvatar = (ImageView) findViewById(R.id.img_my_avatar);
    new Avatar("demo.rocket.chat", "John Doe").into(myAvatar);
  }

  private void closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null) {
      pane.closePane();
    }
  }

  private void setupUserActionToggle() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) findViewById(R.id.toggle_user_action));
    toggleUserAction.setFocusableInTouchMode(false);
    findViewById(R.id.user_info_container).setOnClickListener(view -> {
      toggleUserAction.toggle();
    });
    RxCompoundButton.checkedChanges(toggleUserAction)
        .compose(bindToLifecycle())
        .subscribe(RxView.visibility(findViewById(R.id.user_action_container)));
  }

  private RealmListObserver<RoomSubscription> roomsObserver =
      new RealmListObserver<RoomSubscription>() {
        @Override protected RealmResults<RoomSubscription> queryItems(Realm realm) {
          return realm.where(RoomSubscription.class).findAll();
        }

        @Override protected void onCollectionChanged(List<RoomSubscription> list) {
          roomListManager.setRooms(list);
        }
      };

  private void showRoomFragment(String roomId) {
    showFragment(RoomFragment.create(roomId));
  }

  @Override protected void onResume() {
    super.onResume();
    roomsObserver.sub();
  }

  @Override protected void onPause() {
    roomsObserver.unsub();
    super.onPause();
  }
}
