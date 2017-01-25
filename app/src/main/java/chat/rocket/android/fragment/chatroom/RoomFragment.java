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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout;
import io.realm.Sort;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.rocket.android.fragment.chatroom.dialog.UsersOfRoomDialogFragment;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.LoadMoreScrollListener;
import chat.rocket.android.helper.LogcatIfError;
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
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmModelListAdapter;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
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
    RealmModelListAdapter.OnItemClickListener<PairedMessage> {

  private static final int DIALOG_ID = 1;

  private String hostname;
  private RealmHelper realmHelper;
  private String roomId;
  private RealmObjectObserver<RoomSubscription> roomObserver;
  private String userId;
  private String token;
  private LoadMoreScrollListener scrollListener;
  private RealmObjectObserver<LoadMessageProcedure> procedureObserver;
  private MessageFormManager messageFormManager;
  private RecyclerViewAutoScrollManager autoScrollManager;
  private AbstractNewMessageIndicatorManager newMessageIndicatorManager;
  private Snackbar unreadIndicator;
  private boolean previousUnreadMessageExists;

  private List<AbstractExtraActionItem> extraActionItems;

  public RoomFragment() {
  }

  public static boolean canCreate(RealmHelper realmHelper) {
    User currentUser = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst());
    Session session = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst());
    return currentUser != null && session != null;
  }

  /**
   * create fragment with roomId.
   */
  public static RoomFragment create(String hostname, String roomId) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    args.putString("roomId", roomId);
    RoomFragment fragment = new RoomFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    hostname = args.getString("hostname");
    realmHelper = RealmStore.get(hostname);
    roomId = args.getString("roomId");
    userId = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst()).getId();
    token = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst()).getToken();
    roomObserver = realmHelper
        .createObjectObserver(
            realm -> realm.where(RoomSubscription.class).equalTo(RoomSubscription.ROOM_ID, roomId))
        .setOnUpdateListener(this::onRenderRoom);

    procedureObserver = realmHelper
        .createObjectObserver(realm ->
            realm.where(LoadMessageProcedure.class).equalTo(LoadMessageProcedure.ID, roomId))
        .setOnUpdateListener(this::onUpdateLoadMessageProcedure);

    if (savedInstanceState == null) {
      initialRequest();
    }
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override
  protected void onSetupView() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    MessageListAdapter adapter = (MessageListAdapter) realmHelper.createListAdapter(getContext(),
        realm -> realm.where(Message.class)
            .equalTo(Message.ROOM_ID, roomId)
            .findAllSorted(Message.TIMESTAMP, Sort.DESCENDING),
        context -> new MessageListAdapter(context, hostname, userId, token)
    );
    listView.setAdapter(adapter);
    adapter.setOnItemClickListener(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
        LinearLayoutManager.VERTICAL, true);
    listView.setLayoutManager(layoutManager);
    autoScrollManager = new RecyclerViewAutoScrollManager(layoutManager) {
      @Override
      protected void onAutoScrollMissed() {
        if (newMessageIndicatorManager != null) {
          newMessageIndicatorManager.updateNewMessageCount(getUnreadMessageCount());
        }
      }
    };
    adapter.registerAdapterDataObserver(autoScrollManager);

    scrollListener = new LoadMoreScrollListener(layoutManager, 40) {
      @Override
      public void requestMoreItem() {
        loadMoreRequest();
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

  private Snackbar getUnreadCountIndicatorView(int count) {
    // TODO: replace with another custom View widget, not to hide message composer.
    final String caption = getResources().getString(
        R.string.fmt_dialog_view_latest_message_title, count);

    return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
        .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
  }

  private int getUnreadMessageCount() {
    RoomSubscription room = realmHelper.executeTransactionForRead(realm ->
        realm.where(RoomSubscription.class).equalTo(RoomSubscription.ROOM_ID, roomId).findFirst());
    if (room != null) {
      return realmHelper.executeTransactionForReadResults(realm ->
          realm.where(Message.class)
              .equalTo(Message.ROOM_ID, roomId)
              .greaterThanOrEqualTo(Message.TIMESTAMP, room.getLastSeen())
              .notEqualTo(Message.USER_ID, userId)
              .findAll()).size();
    } else {
      return 0;
    }
  }

  @Override
  public void onDestroyView() {
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    listView.getAdapter().unregisterAdapterDataObserver(autoScrollManager);
    super.onDestroyView();
  }

  @Override
  public void onItemClick(PairedMessage pairedMessage) {
    if (pairedMessage.target != null) {
      final int syncState = pairedMessage.target.getSyncState();
      if (syncState == SyncState.FAILED) {
        final String messageId = pairedMessage.target.getId();
        new AlertDialog.Builder(getContext())
            .setPositiveButton(R.string.resend, (dialog, which) -> {
              realmHelper.executeTransaction(realm ->
                  realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                      .put(Message.ID, messageId)
                      .put(Message.SYNC_STATE, SyncState.NOT_SYNCED))
              ).continueWith(new LogcatIfError());
            })
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.discard, (dialog, which) -> {
              realmHelper.executeTransaction(realm ->
                  realm.where(Message.class)
                      .equalTo(Message.ID, messageId).findAll().deleteAllFromRealm()
              ).continueWith(new LogcatIfError());
            })
            .show();
      }
    }

  }

  private void setupSideMenu() {
    View sideMenu = rootView.findViewById(R.id.room_side_menu);
    sideMenu.findViewById(R.id.btn_users).setOnClickListener(view -> {
      UsersOfRoomDialogFragment.create(roomId, hostname)
          .show(getFragmentManager(), UsersOfRoomDialogFragment.class.getSimpleName());
      closeSideMenuIfNeeded();
    });

    DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
    SlidingPaneLayout pane = (SlidingPaneLayout) getActivity().findViewById(R.id.sliding_pane);
    if (drawerLayout != null && pane != null) {
      RxDrawerLayout.drawerOpen(drawerLayout, GravityCompat.END)
          .compose(bindToLifecycle())
          .subscribe(opened -> {
            try {
              Field fieldSlidable = pane.getClass().getDeclaredField("mCanSlide");
              fieldSlidable.setAccessible(true);
              fieldSlidable.setBoolean(pane, !opened);
            } catch (Exception exception) {
              RCLog.w(exception);
            }
          });
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
    messageFormLayout.setEditTextContentListener(this::onCommitContent);
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
    String uplId = new FileUploadHelper(getContext(), realmHelper)
        .requestUploading(roomId, uri);
    if (!TextUtils.isEmpty(uplId)) {
      FileUploadProgressDialogFragment.create(hostname, roomId, uplId)
          .show(getFragmentManager(), FileUploadProgressDialogFragment.class.getSimpleName());
    } else {
      // show error.
    }
  }

  private void onRenderRoom(RoomSubscription roomSubscription) {
    if (roomSubscription == null) {
      return;
    }

    String type = roomSubscription.getType();
    if (RoomSubscription.TYPE_CHANNEL.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_hashtag_gray_24dp);
    } else if (RoomSubscription.TYPE_PRIVATE.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_lock_gray_24dp);
    } else if (RoomSubscription.TYPE_DIRECT_MESSAGE.equals(type)) {
      setToolbarRoomIcon(R.drawable.ic_at_gray_24dp);
    } else {
      setToolbarRoomIcon(0);
    }
    setToolbarTitle(roomSubscription.getName());

    boolean unreadMessageExists = roomSubscription.isAlert();
    if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
      newMessageIndicatorManager.reset();
    }
    previousUnreadMessageExists = unreadMessageExists;
  }

  private void onUpdateLoadMessageProcedure(LoadMessageProcedure procedure) {
    if (procedure == null) {
      return;
    }
    RecyclerView listView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
    if (listView != null && listView.getAdapter() instanceof MessageListAdapter) {
      MessageListAdapter adapter = (MessageListAdapter) listView.getAdapter();
      final int syncState = procedure.getSyncState();
      final boolean hasNext = procedure.hasNext();
      RCLog.d("hasNext: %s syncstate: %d", hasNext, syncState);
      if (syncState == SyncState.SYNCED || syncState == SyncState.FAILED) {
        scrollListener.setLoadingDone();
        adapter.updateFooter(hasNext, true);
      } else {
        adapter.updateFooter(hasNext, false);
      }
    }
  }

  private void initialRequest() {
    realmHelper.executeTransaction(realm -> {
      realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
          .put(LoadMessageProcedure.ID, roomId)
          .put(LoadMessageProcedure.SYNC_STATE, SyncState.NOT_SYNCED)
          .put(LoadMessageProcedure.COUNT, 100)
          .put(LoadMessageProcedure.RESET, true));
      return null;
    }).onSuccessTask(task -> {
      ConnectivityManager.getInstance(getContext().getApplicationContext())
          .keepAliveServer();
      return task;
    }).continueWith(new LogcatIfError());
  }

  private void loadMoreRequest() {
    realmHelper.executeTransaction(realm -> {
      LoadMessageProcedure procedure = realm.where(LoadMessageProcedure.class)
          .equalTo(LoadMessageProcedure.ID, roomId)
          .beginGroup()
          .equalTo(LoadMessageProcedure.SYNC_STATE, SyncState.SYNCED)
          .or()
          .equalTo(LoadMessageProcedure.SYNC_STATE, SyncState.FAILED)
          .endGroup()
          .equalTo(LoadMessageProcedure.HAS_NEXT, true)
          .findFirst();
      if (procedure != null) {
        procedure.setSyncState(SyncState.NOT_SYNCED);
      }
      return null;
    }).onSuccessTask(task -> {
      ConnectivityManager.getInstance(getContext().getApplicationContext())
          .keepAliveServer();
      return task;
    }).continueWith(new LogcatIfError());
  }

  private void markAsReadIfNeeded() {
    RoomSubscription room = realmHelper.executeTransactionForRead(realm ->
        realm.where(RoomSubscription.class).equalTo(RoomSubscription.ROOM_ID, roomId).findFirst());
    if (room != null && room.isAlert()) {
      new MethodCallHelper(getContext(), hostname).readMessages(roomId)
          .continueWith(new LogcatIfError());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    roomObserver.sub();
    procedureObserver.sub();
    closeSideMenuIfNeeded();
  }

  @Override
  public void onPause() {
    procedureObserver.unsub();
    roomObserver.unsub();
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

  private Task<Void> sendMessage(String messageText) {
    return realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
            .put(Message.ID, UUID.randomUUID().toString())
            .put(Message.SYNC_STATE, SyncState.NOT_SYNCED)
            .put(Message.TIMESTAMP, System.currentTimeMillis())
            .put(Message.ROOM_ID, roomId)
            .put(Message.USER, new JSONObject()
                .put(User.ID, userId))
            .put(Message.MESSAGE, messageText)))
        .onSuccess(_task -> {
          scrollToLatestMessage();
          return null;
        });
  }
}
