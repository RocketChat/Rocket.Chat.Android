package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import org.json.JSONObject;

/**
 * Chat room screen.
 */
public class RoomFragment extends AbstractChatRoomFragment {

  private RealmHelper realmHelper;
  private String roomId;

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
    realmHelper = RealmStore.get(args.getString("serverConfigId"));
    roomId = args.getString("roomId");
  }

  @Override protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override protected void onSetupView() {

    // TODO: just a sample!!
    realmHelper.executeTransaction(realm -> {
      realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
          .put("roomId", roomId)
          .put("syncstate", SyncState.NOT_SYNCED)
          .put("count", 50)
          .put("reset", true));
      return null;
    }).continueWith(new LogcatIfError());
  }

  private RealmObjectObserver<RoomSubscription> roomObserver =
      realmHelper
          .createObjectObserver(realm -> realm.where(RoomSubscription.class).equalTo("rid", roomId))
          .setOnUpdateListener(this::onRenderRoom);

  private void onRenderRoom(RoomSubscription roomSubscription) {
    activityToolbar.setTitle(roomSubscription.getName());
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
