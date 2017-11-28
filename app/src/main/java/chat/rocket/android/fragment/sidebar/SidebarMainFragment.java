package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bolts.Task;
import chat.rocket.android.BuildConfig;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddDirectMessageDialogFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.layouthelper.chatroom.roomlist.ChannelRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.DirectMessageRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.FavoriteRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.LivechatRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.RoomListAdapter;
import chat.rocket.android.layouthelper.chatroom.roomlist.RoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.UnreadRoomListHeader;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View {
  private SidebarMainContract.Presenter presenter;
  private RoomListAdapter adapter;
  private SearchView searchView;
  private TextView loadMoreResultsText;
  private List<RoomSidebar> roomSidebarList = Collections.emptyList();
  private Disposable spotlightDisposable;
  private String hostname;
  private static final String HOSTNAME = "hostname";

  public SidebarMainFragment() {}

  /**
   * build SidebarMainFragment with hostname.
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

    RocketChatCache rocketChatCache = new RocketChatCache(getContext().getApplicationContext());

    presenter = new SidebarMainPresenter(
        hostname,
        new RoomInteractor(new RealmRoomRepository(hostname)),
        userRepository,
        rocketChatCache,
        absoluteUrlHelper,
        new MethodCallHelper(getContext(), hostname),
        new RealmSpotlightRepository(hostname)
    );
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    presenter.bindView(this);
    return view;
  }

  @Override
  public void onDestroyView() {
    presenter.release();
    super.onDestroyView();
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onPause() {

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
      public void onItemClick(RoomSidebar roomSidebar) {
        searchView.setQuery(null, false);
        searchView.clearFocus();
        presenter.onRoomSelected(roomSidebar);
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

    loadMoreResultsText = rootView.findViewById(R.id.text_load_more_results);

    RxSearchView.queryTextChanges(searchView)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(charSequence -> {
                if (spotlightDisposable != null && !spotlightDisposable.isDisposed()) {
                    spotlightDisposable.dispose();
                }
                presenter.disposeSubscriptions();
                if (charSequence.length() == 0) {
                    loadMoreResultsText.setVisibility(View.GONE);
                    adapter.setMode(RoomListAdapter.MODE_ROOM);
                    presenter.bindView(this);
                } else {
                  filterRoomSidebarList(charSequence);
                }
            });

    loadMoreResultsText.setOnClickListener(view -> loadMoreResults());
  }

  @Override
  public void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList) {
    this.roomSidebarList = roomSidebarList;
    adapter.setRoomSidebarList(roomSidebarList);
  }

  @Override
  public void filterRoomSidebarList(CharSequence term) {
      List<RoomSidebar> filteredRoomSidebarList = new ArrayList<>();

      for (RoomSidebar roomSidebar: roomSidebarList) {
          if (roomSidebar.getRoomName().contains(term)) {
              filteredRoomSidebarList.add(roomSidebar);
          }
      }

      if (filteredRoomSidebarList.isEmpty()) {
          loadMoreResults();
      } else {
          loadMoreResultsText.setVisibility(View.VISIBLE);
          adapter.setMode(RoomListAdapter.MODE_ROOM);
          adapter.setRoomSidebarList(filteredRoomSidebarList);
      }
  }

  private void loadMoreResults() {
    spotlightDisposable = presenter.searchSpotlight(searchView.getQuery().toString())
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showSearchSuggestions);
  }

  private void showSearchSuggestions(List<Spotlight> spotlightList) {
    loadMoreResultsText.setVisibility(View.GONE);
    adapter.setMode(RoomListAdapter.MODE_SPOTLIGHT);
    adapter.setSpotlightList(spotlightList);
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

  public void showUserActionContainer(boolean show) {
    rootView.findViewById(R.id.user_action_outer_container)
            .setVisibility(show ? View.VISIBLE : View.GONE);
  }

  public void toggleUserActionContainer(boolean checked) {
    CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
    toggleUserAction.setChecked(checked);
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
  public void show(User user) {
    onRenderCurrentUser(user);
    updateRoomListMode();
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

  private void updateRoomListMode() {
    final List<RoomListHeader> roomListHeaders = new ArrayList<>();

    roomListHeaders.add(new UnreadRoomListHeader(
        getString(R.string.fragment_sidebar_main_unread_rooms_title)
    ));

    roomListHeaders.add(new FavoriteRoomListHeader(
        getString(R.string.fragment_sidebar_main_favorite_title)
    ));

    roomListHeaders.add(new LivechatRoomListHeader(
        getString(R.string.fragment_sidebar_main_livechat_title)
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

  @Override
  public void onLogoutCleanUp() {
    Activity activity = getActivity();
    if (activity != null && activity instanceof MainActivity) {
      ((MainActivity) activity).onLogout();
      presenter.onLogout(task -> {
        if (task.isFaulted()) {
          return Task.forError(task.getError());
        }
        return null;
      });
    }
  }

  private void setupLogoutButton() {
    rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
      closeUserActionContainer();
      // Clear relative data and set new hostname if any.
      presenter.beforeLogoutCleanUp();
      final Activity activity = getActivity();
      if (activity != null && activity instanceof MainActivity) {
        // Clear subscriptions on MainPresenter.
        ((MainActivity) activity).beforeLogoutCleanUp();
      }
    });
  }

  public void clearSearchViewFocus() {
    searchView.clearFocus();
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

}