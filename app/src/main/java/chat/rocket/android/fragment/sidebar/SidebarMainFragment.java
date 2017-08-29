package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import chat.rocket.android.BuildConfig;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddDirectMessageDialogFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.layouthelper.chatroom.roomlist.ChannelRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.DirectMessageRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.FavoriteRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.RoomListAdapter;
import chat.rocket.android.layouthelper.chatroom.roomlist.RoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.UnreadRoomListHeader;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View {
  private SidebarMainContract.Presenter presenter;
  private RoomListAdapter adapter;
  private SearchView searchView;
  private String hostname;
  private static final String HOSTNAME = "hostname";

  public SidebarMainFragment() {}

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

    hostname = getArguments().getString(HOSTNAME);
    RealmUserRepository userRepository = new RealmUserRepository(hostname);

    AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
        hostname,
        new RealmServerInfoRepository(),
        userRepository,
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    presenter = new SidebarMainPresenter(
        hostname,
        new RoomInteractor(new RealmRoomRepository(hostname)),
        userRepository,
        new RocketChatCache(getContext()),
        absoluteUrlHelper,
        new MethodCallHelper(getContext(), hostname),
        new RealmSpotlightRepository(hostname)
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

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupView() {
    setupUserActionToggle();
    setupUserStatusButtons();
    setupLogoutButton();
    setupVersionInfo();

    searchView = rootView.findViewById(R.id.search);

    adapter = new RoomListAdapter();
    adapter.setOnItemClickListener(new RoomListAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(Room room) {
        searchView.clearFocus();
        presenter.onRoomSelected(room);
      }

      @Override
      public void onItemClick(Spotlight spotlight) {
        searchView.setQuery(null, false);
        searchView.clearFocus();
        presenter.onSpotlightSelected(spotlight);
      }
    });

    RecyclerView recyclerView = rootView.findViewById(R.id.room_list_container);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    recyclerView.setAdapter(adapter);

    RxSearchView.queryTextChanges(searchView)
        .compose(bindToLifecycle())
        .debounce(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap(charSequence -> {
          if (charSequence.length() == 0) {
            adapter.setMode(RoomListAdapter.MODE_ROOM);
            return Observable.just(Collections.<Spotlight>emptyList());
          } else {
            adapter.setMode(RoomListAdapter.MODE_SPOTLIGHT);
            return presenter.searchSpotlight(charSequence.toString()).toObservable();
          }
        })
        .subscribe(this::showSearchSuggestions, Logger::report);
  }

  @SuppressLint("RxLeakedSubscription")
  private void setupUserActionToggle() {
    final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
    toggleUserAction.setFocusableInTouchMode(false);

    rootView.findViewById(R.id.user_info_container).setOnClickListener(view -> toggleUserAction.toggle());

    RxCompoundButton.checkedChanges(toggleUserAction)
        .compose(bindToLifecycle())
        .subscribe(
            this::showUserActionContainer,
            Logger::report
        );
  }

  private void showUserActionContainer(boolean show) {
    rootView.findViewById(R.id.user_action_outer_container)
            .setVisibility(show ? View.VISIBLE : View.GONE);
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
  public void showRoomList(@NonNull List<Room> roomList) {
    adapter.setRooms(roomList);
  }

  @Override
  public void showUserStatus(@NonNull User user) {
    adapter.setUserStatus(user);
  }

  @Override
  public void show(User user) {
    onRenderCurrentUser(user);
    updateRoomListMode(user);
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
    if (user != null) {
      UserRenderer userRenderer = new UserRenderer(user);
      userRenderer.showAvatar(rootView.findViewById(R.id.current_user_avatar), hostname);
      userRenderer.showUsername(rootView.findViewById(R.id.current_user_name));
      userRenderer.showStatusColor(rootView.findViewById(R.id.current_user_status));
    }
  }

  private void updateRoomListMode(User user) {
    final List<RoomListHeader> roomListHeaders = new ArrayList<>();

    if (user != null && user.getSettings() != null && user.getSettings().getPreferences() != null
        && user.getSettings().getPreferences().isUnreadRoomsMode()) {
      roomListHeaders.add(new UnreadRoomListHeader(
          getString(R.string.fragment_sidebar_main_unread_rooms_title)
      ));
    }

    roomListHeaders.add(new FavoriteRoomListHeader(
        getString(R.string.fragment_sidebar_main_favorite_title)
    ));

    roomListHeaders.add(new ChannelRoomListHeader(
        getString(R.string.fragment_sidebar_main_channels_title),
        () -> showAddRoomDialog(AddChannelDialogFragment.create(hostname))
    ));
    roomListHeaders.add(new DirectMessageRoomListHeader(
        getString(R.string.fragment_sidebar_main_direct_messages_title),
        () -> showAddRoomDialog(AddDirectMessageDialogFragment.create(hostname))
    ));

    adapter.setRoomListHeaders(roomListHeaders);
  }

  private void setupLogoutButton() {
    rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
      presenter.onLogout();
      closeUserActionContainer();
      // destroy Activity on logout to be able to recreate most of the environment
      this.getActivity().finish();
    });
  }

  public void closeUserActionContainer() {
    final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
    if (toggleUserAction != null && toggleUserAction.isChecked()) {
      toggleUserAction.setChecked(false);
    }
  }

  private void setupVersionInfo() {
    TextView versionInfoView = rootView.findViewById(R.id.version_info);
    versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
  }

  private void showAddRoomDialog(DialogFragment dialog) {
    dialog.show(getFragmentManager(), "AbstractAddRoomDialogFragment");
  }

  private void showSearchSuggestions(List<Spotlight> spotlightList) {
    adapter.setSpotlightList(spotlightList);
  }
}