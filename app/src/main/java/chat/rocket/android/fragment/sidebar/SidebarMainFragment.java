package chat.rocket.android.fragment.sidebar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;

import java.util.List;
import chat.rocket.android.BuildConfig;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.fragment.sidebar.dialog.AbstractAddRoomDialogFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddDirectMessageDialogFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.RoomListManager;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import chat.rocket.android.widget.RocketChatAvatar;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View {

  private static final String HOSTNAME = "hostname";

  private SidebarMainContract.Presenter presenter;

  private RoomListManager roomListManager;

  private String hostname;

  public SidebarMainFragment() {
  }

  /**
   * create SidebarMainFragment with hostname.
   */
  public static SidebarMainFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString(HOSTNAME, hostname);

    SidebarMainFragment fragment = new SidebarMainFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    hostname = args == null ? null : args.getString(HOSTNAME);

    presenter = new SidebarMainPresenter(
        hostname,
        new RoomInteractor(new RealmRoomRepository(hostname)),
        new RealmUserRepository(hostname),
        TextUtils.isEmpty(hostname) ? null : new MethodCallHelper(getContext(), hostname)
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
  }

  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_sidebar_main;
  }

  @Override
  protected void onSetupView() {
    setupUserActionToggle();
    setupUserStatusButtons();
    setupLogoutButton();
    setupAddChannelButton();
    setupVersionInfo();

    roomListManager = new RoomListManager(
        rootView.findViewById(R.id.unread_title),
        (LinearLayout) rootView.findViewById(R.id.unread_container),
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
    RxJavaInterop.toV2Flowable(RxCompoundButton.checkedChanges(toggleUserAction))
        .compose(bindToLifecycle())
        .subscribe(aBoolean -> {
          rootView.findViewById(R.id.user_action_outer_container)
              .setVisibility(aBoolean ? View.VISIBLE : View.GONE);
        });
  }

  private void setupUserStatusButtons() {
    rootView.findViewById(R.id.btn_status_online).setOnClickListener(view -> {
      presenter.onUserOnline();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_away).setOnClickListener(view -> {
      presenter.onUserAway();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_busy).setOnClickListener(view -> {
      presenter.onUserBusy();
      closeUserActionContainer();
    });
    rootView.findViewById(R.id.btn_status_invisible).setOnClickListener(view -> {
      presenter.onUserOffline();
      closeUserActionContainer();
    });
  }

  private void onRenderCurrentUser(User user) {
    if (user != null && !TextUtils.isEmpty(hostname)) {
      new UserRenderer(getContext(), user)
          .avatarInto((RocketChatAvatar) rootView.findViewById(R.id.current_user_avatar), hostname)
          .usernameInto((TextView) rootView.findViewById(R.id.current_user_name))
          .statusColorInto((ImageView) rootView.findViewById(R.id.current_user_status));
    }
  }

  private void updateRoomListMode(User user) {
    if (user == null || user.getSettings() == null || user.getSettings().getPreferences() == null) {
      return;
    }
    roomListManager.setUnreadRoomMode(user.getSettings().getPreferences().isUnreadRoomsMode());
  }

  private void setupLogoutButton() {
    rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
      presenter.onLogout();
      closeUserActionContainer();
    });
  }

  private void closeUserActionContainer() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) rootView.findViewById(R.id.toggle_user_action));
    if (toggleUserAction != null && toggleUserAction.isChecked()) {
      toggleUserAction.setChecked(false);
    }
  }

  private void setupAddChannelButton() {
    rootView.findViewById(R.id.btn_add_channel).setOnClickListener(view -> {
      showAddRoomDialog(AddChannelDialogFragment.create(hostname));
    });

    rootView.findViewById(R.id.btn_add_direct_message).setOnClickListener(view -> {
      showAddRoomDialog(AddDirectMessageDialogFragment.create(hostname));
    });
  }

  private void setupVersionInfo() {
    TextView versionInfoView = (TextView) rootView.findViewById(R.id.version_info);
    versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
  }

  private void showAddRoomDialog(DialogFragment dialog) {
    dialog.show(getFragmentManager(), AbstractAddRoomDialogFragment.class.getSimpleName());
  }

  @Override
  public void showScreen() {
    rootView.setVisibility(View.VISIBLE);
  }

  @Override
  public void showEmptyScreen() {
    rootView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void showRoomList(List<Room> roomList) {
    roomListManager.setRooms(roomList);
  }

  @Override
  public void showUser(User user) {
    onRenderCurrentUser(user);
    updateRoomListMode(user);
  }
}
