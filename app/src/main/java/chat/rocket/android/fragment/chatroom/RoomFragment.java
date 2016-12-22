package chat.rocket.android.fragment.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.UUID;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.rocket.android.fragment.chatroom.dialog.MessageSelectionDialogFragment;
import chat.rocket.android.fragment.chatroom.dialog.UsersOfRoomDialogFragment;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.LoadMoreScrollListener;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.MessageComposerManager;
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter;
import chat.rocket.android.layouthelper.chatroom.PairedMessage;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.message.AbstractMessageSpec;
import chat.rocket.android.message.AudioUploadMessageSpec;
import chat.rocket.android.message.AbstractUploadMessageSpec;
import chat.rocket.android.message.ImageUploadMessageSpec;
import chat.rocket.android.message.VideoUploadMessageSpec;
import chat.rocket.android.model.ServerConfig;
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
import chat.rocket.android.service.RocketChatService;
import chat.rocket.android.widget.message.MessageComposer;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment
    implements OnBackPressListener, RealmModelListAdapter.OnItemClickListener<PairedMessage> {

  private String serverConfigId;
  private RealmHelper realmHelper;
  private String roomId;
  private RealmObjectObserver<RoomSubscription> roomObserver;
  private String hostname;
  private String userId;
  private String token;
  private LoadMoreScrollListener scrollListener;
  private RealmObjectObserver<LoadMessageProcedure> procedureObserver;
  private MessageComposerManager messageComposerManager;

  private MessageSelectionDialogFragment.ClickListener messageSelectionClickListener =
      messageSpec -> RoomFragmentPermissionsDispatcher
          .onMessageSpecSelectedWithCheck(RoomFragment.this, messageSpec);

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
  public static RoomFragment create(String serverConfigId, String roomId) {
    Bundle args = new Bundle();
    args.putString("serverConfigId", serverConfigId);
    args.putString("roomId", roomId);
    RoomFragment fragment = new RoomFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    serverConfigId = args.getString("serverConfigId");
    realmHelper = RealmStore.get(serverConfigId);
    roomId = args.getString("roomId");
    hostname = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class)
            .equalTo("serverConfigId", serverConfigId)
            .isNotNull("hostname")
            .findFirst()).getHostname();
    userId = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst()).getId();
    token = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst()).getToken();
    roomObserver = realmHelper
        .createObjectObserver(realm -> realm.where(RoomSubscription.class).equalTo("rid", roomId))
        .setOnUpdateListener(this::onRenderRoom);

    procedureObserver = realmHelper
        .createObjectObserver(realm ->
            realm.where(LoadMessageProcedure.class).equalTo("roomId", roomId))
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
            .equalTo("rid", roomId)
            .findAllSorted("ts", Sort.DESCENDING),
        context -> new MessageListAdapter(context, hostname, userId, token)
    );
    listView.setAdapter(adapter);
    adapter.setOnItemClickListener(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
        LinearLayoutManager.VERTICAL, true);
    listView.setLayoutManager(layoutManager);

    scrollListener = new LoadMoreScrollListener(layoutManager, 40) {
      @Override
      public void requestMoreItem() {
        loadMoreRequest();
      }
    };
    listView.addOnScrollListener(scrollListener);

    setupSideMenu();
    setupMessageComposer();
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
                      .put("_id", messageId)
                      .put("syncstate", SyncState.NOT_SYNCED))
              ).continueWith(new LogcatIfError());
            })
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.discard, (dialog, which) -> {
              realmHelper.executeTransaction(realm ->
                  realm.where(Message.class)
                      .equalTo("_id", messageId).findAll().deleteAllFromRealm()
              ).continueWith(new LogcatIfError());
            })
            .show();
      }
    }

  }

  private void setupSideMenu() {
    View sideMenu = rootView.findViewById(R.id.room_side_menu);
    sideMenu.findViewById(R.id.btn_users).setOnClickListener(view -> {
      UsersOfRoomDialogFragment.create(serverConfigId, roomId, hostname)
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
    final MessageComposer messageComposer =
        (MessageComposer) rootView.findViewById(R.id.message_composer);
    messageComposerManager = new MessageComposerManager(messageComposer);
    messageComposerManager.setSendMessageCallback(messageText ->
        realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                .put("_id", UUID.randomUUID().toString())
                .put("syncstate", SyncState.NOT_SYNCED)
                .put("ts", System.currentTimeMillis())
                .put("rid", roomId)
                .put("msg", messageText))));
    messageComposerManager.setExtrasPickerListener(() -> {
      MessageSelectionDialogFragment fragment = MessageSelectionDialogFragment.create();

      fragment.addMessageSpec(new ImageUploadMessageSpec());
      fragment.addMessageSpec(new AudioUploadMessageSpec());
      fragment.addMessageSpec(new VideoUploadMessageSpec());

      fragment.setListener(messageSelectionClickListener);

      fragment.show(getFragmentManager(), MessageSelectionDialogFragment.TAG);

      closeSideMenuIfNeeded();
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode != AbstractUploadMessageSpec.RC_UPL || resultCode != Activity.RESULT_OK) {
      return;
    }

    if (data == null || data.getData() == null) {
      return;
    }

    String uplId = new FileUploadHelper(getContext(), realmHelper)
        .requestUploading(roomId, data.getData());
    if (!TextUtils.isEmpty(uplId)) {
      FileUploadProgressDialogFragment.create(serverConfigId, roomId, uplId)
          .show(getFragmentManager(), FileUploadProgressDialogFragment.class.getSimpleName());
    } else {
      //show error.
    }
  }

  private void onRenderRoom(RoomSubscription roomSubscription) {
    if (roomSubscription == null) {
      return;
    }

    String type = roomSubscription.getType();
    if (RoomSubscription.TYPE_CHANNEL.equals(type)) {
      activityToolbar.setNavigationIcon(R.drawable.ic_hashtag_gray_24dp);
    } else if (RoomSubscription.TYPE_PRIVATE.equals(type)) {
      activityToolbar.setNavigationIcon(R.drawable.ic_lock_gray_24dp);
    } else if (RoomSubscription.TYPE_DIRECT_MESSAGE.equals(type)) {
      activityToolbar.setNavigationIcon(R.drawable.ic_at_gray_24dp);
    } else {
      activityToolbar.setNavigationIcon(null);
    }
    activityToolbar.setTitle(roomSubscription.getName());
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
          .put("roomId", roomId)
          .put("syncstate", SyncState.NOT_SYNCED)
          .put("count", 100)
          .put("reset", true));
      return null;
    }).onSuccessTask(task -> {
      RocketChatService.keepAlive(getContext());
      return task;
    }).continueWith(new LogcatIfError());
  }

  private void loadMoreRequest() {
    realmHelper.executeTransaction(realm -> {
      LoadMessageProcedure procedure = realm.where(LoadMessageProcedure.class)
          .equalTo("roomId", roomId)
          .beginGroup()
          .equalTo("syncstate", SyncState.SYNCED)
          .or()
          .equalTo("syncstate", SyncState.FAILED)
          .endGroup()
          .equalTo("hasNext", true)
          .findFirst();
      if (procedure != null) {
        procedure.setSyncState(SyncState.NOT_SYNCED);
      }
      return null;
    }).onSuccessTask(task -> {
      RocketChatService.keepAlive(getContext());
      return task;
    }).continueWith(new LogcatIfError());
  }

  private void markAsReadIfNeeded() {
    RoomSubscription room = realmHelper.executeTransactionForRead(realm ->
        realm.where(RoomSubscription.class).equalTo("rid", roomId).findFirst());
    if (room != null && room.isAlert()) {
      new MethodCallHelper(getContext(), serverConfigId).readMessages(roomId)
          .continueWith(new LogcatIfError());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    roomObserver.sub();
    procedureObserver.sub();
    closeSideMenuIfNeeded();
    markAsReadIfNeeded();
  }

  @Override
  public void onPause() {
    procedureObserver.unsub();
    roomObserver.unsub();
    super.onPause();
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
  protected void onMessageSpecSelected(AbstractMessageSpec messageSpec) {
    messageSpec.onSelect(RoomFragment.this);
  }
}
