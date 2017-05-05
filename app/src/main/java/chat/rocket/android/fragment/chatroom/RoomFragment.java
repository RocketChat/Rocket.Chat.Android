package chat.rocket.android.fragment.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.fernandocejas.arrow.optional.Optional;
import com.jakewharton.rxbinding2.support.v4.widget.RxDrawerLayout;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.rocket.android.fragment.chatroom.dialog.MessageOptionsDialogFragment;
import chat.rocket.android.fragment.chatroom.dialog.UsersOfRoomDialogFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.LoadMoreScrollListener;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.helper.RecyclerViewAutoScrollManager;
import chat.rocket.android.helper.RecyclerViewScrolledToBottomListener;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.AbstractNewMessageIndicatorManager;
import chat.rocket.android.layouthelper.chatroom.MessageFormManager;
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter;
import chat.rocket.android.layouthelper.chatroom.PairedMessage;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.rocket.android.layouthelper.extra_action.MessageExtraActionBehavior;
import chat.rocket.android.layouthelper.extra_action.upload.AbstractUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.AudioUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.ImageUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.VideoUploadActionItem;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.renderer.RocketChatUserStatusProvider;
import chat.rocket.android.service.temp.DeafultTempSpotlightRoomCaller;
import chat.rocket.android.service.temp.DefaultTempSpotlightUserCaller;
import chat.rocket.android.widget.message.autocomplete.AutocompleteManager;
import chat.rocket.android.widget.message.autocomplete.channel.ChannelSource;
import chat.rocket.android.widget.message.autocomplete.user.UserSource;
import chat.rocket.core.interactors.AutocompleteChannelInteractor;
import chat.rocket.core.interactors.AutocompleteUserInteractor;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import chat.rocket.android.layouthelper.chatroom.ModelListAdapter;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.widget.internal.ExtraActionPickerDialogFragment;
import chat.rocket.android.widget.message.MessageFormLayout;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment
    implements OnBackPressListener, ExtraActionPickerDialogFragment.Callback,
    ModelListAdapter.OnItemClickListener<PairedMessage>,
    ModelListAdapter.OnItemLongClickListener<PairedMessage>, RoomContract.View {

  private static final int DIALOG_ID = 1;
  private static final String HOSTNAME = "hostname";
  private static final String ROOM_ID = "roomId";

  private String hostname;
  private String roomId;
  private LoadMoreScrollListener scrollListener;
  private MessageFormManager messageFormManager;
  private RecyclerViewAutoScrollManager autoScrollManager;
  protected AbstractNewMessageIndicatorManager newMessageIndicatorManager;
  protected Snackbar unreadIndicator;
  private boolean previousUnreadMessageExists;
  private MessageListAdapter adapter;
  private AutocompleteManager autocompleteManager;

  private List<AbstractExtraActionItem> extraActionItems;

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  protected RoomContract.Presenter presenter;

  private RealmRoomRepository roomRepository;
  private RealmUserRepository userRepository;
  private MethodCallHelper methodCallHelper;
  private AbsoluteUrlHelper absoluteUrlHelper;

  private Message edittingMessage = null;

  public RoomFragment() {
  }

  /**
   * create fragment with roomId.
   */
  public static RoomFragment create(String hostname, String roomId) {
    Bundle args = new Bundle();
    args.putString(HOSTNAME, hostname);
    args.putString(ROOM_ID, roomId);

    RoomFragment fragment = new RoomFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    hostname = args.getString(HOSTNAME);
    roomId = args.getString(ROOM_ID);

    roomRepository = new RealmRoomRepository(hostname);

    MessageInteractor messageInteractor = new MessageInteractor(
        new RealmMessageRepository(hostname),
        roomRepository
    );

    userRepository = new RealmUserRepository(hostname);

    absoluteUrlHelper = new AbsoluteUrlHelper(
        hostname,
        new RealmServerInfoRepository(),
        userRepository,
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    methodCallHelper = new MethodCallHelper(getContext(), hostname);

    presenter = new RoomPresenter(
        roomId,
        userRepository,
        messageInteractor,
        roomRepository,
        absoluteUrlHelper,
        methodCallHelper,
        ConnectivityManager.getInstance(getContext())
    );

    if (savedInstanceState == null) {
      presenter.loadMessages();
    }
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override
  protected void onSetupView() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    adapter = new MessageListAdapter(getContext());
    listView.setAdapter(adapter);
    adapter.setOnItemClickListener(this);
    adapter.setOnItemLongClickListener(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
        LinearLayoutManager.VERTICAL, true);
    listView.setLayoutManager(layoutManager);
    autoScrollManager = new RecyclerViewAutoScrollManager(layoutManager) {
      @Override
      protected void onAutoScrollMissed() {
        if (newMessageIndicatorManager != null) {
          presenter.onUnreadCount();
        }
      }
    };
    adapter.registerAdapterDataObserver(autoScrollManager);

    scrollListener = new LoadMoreScrollListener(layoutManager, 40) {
      @Override
      public void requestMoreItem() {
        presenter.loadMoreMessages();
      }
    };
    listView.addOnScrollListener(scrollListener);
    listView.addOnScrollListener(
        new RecyclerViewScrolledToBottomListener(layoutManager, 1, this::markAsReadIfNeeded));

    newMessageIndicatorManager = new AbstractNewMessageIndicatorManager() {
      @Override
      protected void onShowIndicator(int count, boolean onlyAlreadyShown) {
        if ((onlyAlreadyShown && unreadIndicator != null && unreadIndicator.isShown())
            || !onlyAlreadyShown) {
          unreadIndicator = getUnreadCountIndicatorView(count);
          unreadIndicator.show();
        }
      }

      @Override
      protected void onHideIndicator() {
        if (unreadIndicator != null && unreadIndicator.isShown()) {
          unreadIndicator.dismiss();
        }
      }
    };

    setupSideMenu();
    setupMessageComposer();
    setupMessageActions();
  }

  private void setupMessageActions() {
    extraActionItems = new ArrayList<>(3); // fixed number as of now
    extraActionItems.add(new ImageUploadActionItem());
    extraActionItems.add(new AudioUploadActionItem());
    extraActionItems.add(new VideoUploadActionItem());
  }

  private void scrollToLatestMessage() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    if (listView != null) {
      listView.scrollToPosition(0);
    }
  }

  protected Snackbar getUnreadCountIndicatorView(int count) {
    // TODO: replace with another custom View widget, not to hide message composer.
    final String caption = getResources().getString(
        R.string.fmt_dialog_view_latest_message_title, count);

    return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
        .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
  }

  @Override
  public void onDestroyView() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    if (listView != null) {
      RecyclerView.Adapter adapter = listView.getAdapter();
      if (adapter != null) {
        adapter.unregisterAdapterDataObserver(autoScrollManager);
      }
    }

    compositeDisposable.clear();

    if (autocompleteManager != null) {
      autocompleteManager.dispose();
      autocompleteManager = null;
    }

    super.onDestroyView();
  }

  @Override
  public void onItemClick(PairedMessage pairedMessage) {
    presenter.onMessageSelected(pairedMessage.target);
  }

  @Override
  public boolean onItemLongClick(PairedMessage pairedMessage) {
    MessageOptionsDialogFragment messageOptionsDialogFragment = MessageOptionsDialogFragment
        .create(pairedMessage.target);

    messageOptionsDialogFragment.setOnMessageOptionSelectedListener(message -> {
      messageOptionsDialogFragment.dismiss();
      onEditMessage(message);
    });

    messageOptionsDialogFragment.show(getChildFragmentManager(), "MessageOptionsDialogFragment");
    return true;
  }

  private void setupSideMenu() {
    View sideMenu = rootView.findViewById(R.id.room_side_menu);
    sideMenu.findViewById(R.id.btn_users).setOnClickListener(view -> {
      UsersOfRoomDialogFragment.create(roomId, hostname)
          .show(getFragmentManager(), "UsersOfRoomDialogFragment");
      closeSideMenuIfNeeded();
    });

    DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
    SlidingPaneLayout pane = (SlidingPaneLayout) getActivity().findViewById(R.id.sliding_pane);
    if (drawerLayout != null && pane != null) {
      compositeDisposable.add(RxDrawerLayout.drawerOpen(drawerLayout, GravityCompat.END)
          .compose(bindToLifecycle())
          .subscribe(
              opened -> {
                try {
                  Field fieldSlidable = pane.getClass().getDeclaredField("mCanSlide");
                  fieldSlidable.setAccessible(true);
                  fieldSlidable.setBoolean(pane, !opened);
                } catch (Exception exception) {
                  RCLog.w(exception);
                }
              },
              Logger::report
          )
      );
    }
  }

  private boolean closeSideMenuIfNeeded() {
    DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
    if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
      drawerLayout.closeDrawer(GravityCompat.END);
      return true;
    }
    return false;
  }

  private void setupMessageComposer() {
    final MessageFormLayout messageFormLayout =
        (MessageFormLayout) rootView.findViewById(R.id.message_composer);
    messageFormManager =
        new MessageFormManager(messageFormLayout, this::showExtraActionSelectionDialog);
    messageFormManager.setSendMessageCallback(this::sendMessage);
    messageFormLayout.setEditTextCommitContentListener(this::onCommitContent);

    autocompleteManager =
        new AutocompleteManager((ViewGroup) rootView.findViewById(R.id.message_list_root));

    autocompleteManager.registerSource(
        new ChannelSource(
            new AutocompleteChannelInteractor(
                roomRepository,
                new RealmSpotlightRoomRepository(hostname),
                new DeafultTempSpotlightRoomCaller(methodCallHelper)
            ),
            AndroidSchedulers.from(BackgroundLooper.get()),
            AndroidSchedulers.mainThread()
        )
    );

    Disposable disposable = Single.zip(
        absoluteUrlHelper.getRocketChatAbsoluteUrl(),
        roomRepository.getById(roomId).first(Optional.absent()),
        Pair::create
    )
        .subscribe(
            pair -> {
              if (pair.first.isPresent() && pair.second.isPresent()) {
                autocompleteManager.registerSource(
                    new UserSource(
                        new AutocompleteUserInteractor(
                            pair.second.get(),
                            userRepository,
                            new RealmMessageRepository(hostname),
                            new RealmSpotlightUserRepository(hostname),
                            new DefaultTempSpotlightUserCaller(methodCallHelper)
                        ),
                        pair.first.get(),
                        RocketChatUserStatusProvider.getInstance(),
                        AndroidSchedulers.from(BackgroundLooper.get()),
                        AndroidSchedulers.mainThread()
                    )
                );
              }
            },
            throwable -> {
            }
        );

    compositeDisposable.add(disposable);

    autocompleteManager.bindTo(
        messageFormLayout.getEditText(),
        messageFormLayout
    );
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode != AbstractUploadActionItem.RC_UPL || resultCode != Activity.RESULT_OK) {
      return;
    }

    if (data == null || data.getData() == null) {
      return;
    }

    uploadFile(data.getData());
  }

  private void uploadFile(Uri uri) {
    String uplId = new FileUploadHelper(getContext(), RealmStore.get(hostname))
        .requestUploading(roomId, uri);
    if (!TextUtils.isEmpty(uplId)) {
      FileUploadProgressDialogFragment.create(hostname, roomId, uplId)
          .show(getFragmentManager(), "FileUploadProgressDialogFragment");
    } else {
      // show error.
    }
  }

  private void markAsReadIfNeeded() {
    presenter.onMarkAsRead();
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
    closeSideMenuIfNeeded();
  }

  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  private void showExtraActionSelectionDialog() {
    final DialogFragment fragment = ExtraActionPickerDialogFragment
        .create(new ArrayList<>(extraActionItems));
    fragment.setTargetFragment(this, DIALOG_ID);
    fragment.show(getFragmentManager(), "ExtraActionPickerDialogFragment");
  }

  @Override
  public void onItemSelected(int itemId) {
    for (AbstractExtraActionItem extraActionItem : extraActionItems) {
      if (extraActionItem.getItemId() == itemId) {
        RoomFragmentPermissionsDispatcher
            .onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
        return;
      }
    }
  }

  @Override
  public boolean onBackPressed() {
    if (edittingMessage != null) {
      edittingMessage = null;
      messageFormManager.clearComposingText();
      return true;
    }
    return closeSideMenuIfNeeded();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    RoomFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
  }

  @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
  protected void onExtraActionSelected(MessageExtraActionBehavior action) {
    action.handleItemSelectedOnFragment(RoomFragment.this);
  }

  private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                  Bundle opts, String[] supportedMimeTypes) {
    boolean supported = false;
    for (final String mimeType : supportedMimeTypes) {
      if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
        supported = true;
        break;
      }
    }

    if (!supported) {
      return false;
    }

    if (BuildCompat.isAtLeastNMR1()
        && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
      try {
        inputContentInfo.requestPermission();
      } catch (Exception e) {
        return false;
      }
    }

    Uri linkUri = inputContentInfo.getLinkUri();
    if (linkUri == null) {
      return false;
    }

    sendMessage(linkUri.toString());

    try {
      inputContentInfo.releasePermission();
    } catch (Exception e) {
    }

    return true;
  }

  private void sendMessage(String messageText) {
    if (edittingMessage == null) {
      presenter.sendMessage(messageText);
    } else {
      presenter.updateMessage(edittingMessage, messageText);
    }
  }

  @Override
  public void setupWith(RocketChatAbsoluteUrl rocketChatAbsoluteUrl) {
    adapter.setAbsoluteUrl(rocketChatAbsoluteUrl);
  }

  @Override
  public void render(Room room) {
    String type = room.getType();
    if (Room.TYPE_CHANNEL.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_hashtag_gray_24dp);
    } else if (Room.TYPE_PRIVATE.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_lock_gray_24dp);
    } else if (Room.TYPE_DIRECT_MESSAGE.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_at_gray_24dp);
    } else {
      setToolbarRoomIcon(0);
    }
    setToolbarTitle(room.getName());

    boolean unreadMessageExists = room.isAlert();
    if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
      newMessageIndicatorManager.reset();
    }
    previousUnreadMessageExists = unreadMessageExists;
  }

  @Override
  public void updateHistoryState(boolean hasNext, boolean isLoaded) {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    if (listView == null || !(listView.getAdapter() instanceof MessageListAdapter)) {
      return;
    }

    MessageListAdapter adapter = (MessageListAdapter) listView.getAdapter();
    if (isLoaded) {
      scrollListener.setLoadingDone();
    }
    adapter.updateFooter(hasNext, isLoaded);
  }

  @Override
  public void onMessageSendSuccessfully() {
    scrollToLatestMessage();
    messageFormManager.onMessageSend();
    edittingMessage = null;
  }

  @Override
  public void showUnreadCount(int count) {
    newMessageIndicatorManager.updateNewMessageCount(count);
  }

  @Override
  public void showMessages(List<Message> messages) {
    if (adapter == null) {
      return;
    }
    adapter.updateData(messages);
  }

  @Override
  public void showMessageSendFailure(Message message) {
    new AlertDialog.Builder(getContext())
        .setPositiveButton(R.string.resend,
            (dialog, which) -> presenter.resendMessage(message))
        .setNegativeButton(android.R.string.cancel, null)
        .setNeutralButton(R.string.discard,
            (dialog, which) -> presenter.deleteMessage(message))
        .show();
  }

  @Override
  public void autoloadImages() {
    adapter.setAutoloadImages(true);
  }

  @Override
  public void manualLoadImages() {
    adapter.setAutoloadImages(false);
  }

  private void onEditMessage(Message message) {
    edittingMessage = message;
    messageFormManager.setEditMessage(message.getMessage());
  }
}
