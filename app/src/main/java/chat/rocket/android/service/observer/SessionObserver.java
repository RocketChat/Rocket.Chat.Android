package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.MethodCallHelper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

/**
 * Observes user is logged into server.
 */
public class SessionObserver extends AbstractModelObserver<Session> {
  private final MethodCallHelper methodCall;
  private int count;

  /**
   * constructor.
   */
  public SessionObserver(Context context, RealmHelper realmHelper, RocketChatWebSocketAPI api) {
    super(context, realmHelper, api);
    methodCall = new MethodCallHelper(realmHelper, api);
    count = 0;
  }

  @Override public RealmResults<Session> queryItems(Realm realm) {
    return realm.where(Session.class)
        .isNotNull("token")
        .equalTo("tokenVerified", true)
        .isNull("error")
        .findAll();
  }

  @Override public void onUpdateResults(List<Session> results) {
    int origCount = count;
    count = results.size();
    if (origCount > 0 && count > 0) {
      return;
    }

    if (count == 0) {
      if (origCount > 0) {
        onLogout();
      }
      return;
    }

    if (origCount == 0 && count > 0) {
      onLogin();
    }
  }

  @DebugLog private void onLogin() {
    realmHelper.executeTransaction(realm -> {
      realm.delete(RoomSubscription.class);
      return null;
    }).onSuccessTask(_task -> methodCall.getRooms())
        .continueWith(new LogcatIfError());

  }

  @DebugLog private void onLogout() {

  }
}
