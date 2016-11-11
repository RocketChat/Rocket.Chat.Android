package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import chat.rocket.android.R;
import chat.rocket.android.model.RoomSubscription;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;

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
