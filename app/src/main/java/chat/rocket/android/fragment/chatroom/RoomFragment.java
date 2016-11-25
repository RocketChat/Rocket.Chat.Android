package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmModelListView;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import org.json.JSONObject;

/**
 * Chat room screen.
 */
public class RoomFragment extends AbstractChatRoomFragment {

  private RealmHelper realmHelper;
  private String roomId;
  private RealmObjectObserver<RoomSubscription> roomObserver;
  private String hostname;

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

  public RoomFragment() {
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    String serverConfigId = args.getString("serverConfigId");
    realmHelper = RealmStore.get(serverConfigId);
    roomId = args.getString("roomId");
    hostname = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class)
            .equalTo("serverConfigId", serverConfigId)
            .isNotNull("hostname")
            .findFirst()).getHostname();
    roomObserver = realmHelper
        .createObjectObserver(realm -> realm.where(RoomSubscription.class).equalTo("rid", roomId))
        .setOnUpdateListener(this::onRenderRoom);

    initialRequest();
  }

  @Override protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override protected void onSetupView() {
    RealmModelListView listView = (RealmModelListView) rootView.findViewById(R.id.listview);

    realmHelper.bindListView(listView,
        realm -> realm.where(Message.class)
            .equalTo("rid", roomId)
            .findAllSorted("ts", Sort.DESCENDING),
        context -> new MessageListAdapter(context, hostname));

    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
        LinearLayoutManager.VERTICAL, true);
    listView.setLayoutManager(layoutManager);
  }

  private void onRenderRoom(RoomSubscription roomSubscription) {
    activityToolbar.setTitle(roomSubscription.getName());
  }

  private void initialRequest() {
    realmHelper.executeTransaction(realm -> {
      realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
          .put("roomId", roomId)
          .put("syncstate", SyncState.NOT_SYNCED)
          .put("count", 50)
          .put("reset", true));
      return null;
    }).continueWith(new LogcatIfError());
  }

  private RealmResults<Message> queryItems(Realm realm) {
    return realm.where(Message.class).equalTo("rid", roomId).findAllSorted("ts");
  }

  @Override public void onResume() {
    super.onResume();
    roomObserver.sub();
  }

  @Override public void onPause() {
    roomObserver.unsub();
    super.onPause();
  }
}
