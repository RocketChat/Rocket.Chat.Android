package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.LoadMessageProcedure;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.RoomSubscription;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

/**
 * Chat room screen.
 */
public class RoomFragment extends AbstractChatRoomFragment {

  private String roomId;

  /**
   * create fragment with roomId.
   */
  public static RoomFragment create(String roomId) {
    Bundle args = new Bundle();
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
    roomId = args.getString("roomId");
  }

  @Override protected int getLayout() {
    return R.layout.fragment_room;
  }

  @Override protected void onSetupView() {

    // TODO: just a sample!!
    RealmHelperBolts.executeTransaction(realm -> {
      final String serverConfigId = realm.where(RoomSubscription.class)
          .equalTo("rid", roomId).findFirst().getServerConfigId();
      realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
          .put("serverConfigId", serverConfigId)
          .put("roomId", roomId)
          .put("syncstate", SyncState.NOT_SYNCED)
          .put("count", 50)
          .put("reset", true));
      return null;
    }).continueWith(new LogcatIfError());
  }

  private RealmObjectObserver<RoomSubscription> roomObserver =
      new RealmObjectObserver<RoomSubscription>() {
        @Override protected RealmQuery<RoomSubscription> query(Realm realm) {
          return realm.where(RoomSubscription.class).equalTo("rid", roomId);
        }

        @Override protected void onChange(RoomSubscription roomSubscription) {
          onRenderRoom(roomSubscription);
        }
      };

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
