package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.MethodCallHelper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 * Observes user is logged into server.
 */
public class SessionObserver extends AbstractModelObserver<ServerConfig> {
  private final MethodCallHelper methodCall;
  private int count;

  /**
   * constructor.
   */
  public SessionObserver(Context context, String serverConfigId, RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
    methodCall = new MethodCallHelper(serverConfigId, api);
    count = 0;
  }

  @Override protected RealmResults<ServerConfig> queryItems(Realm realm) {
    return realm.where(ServerConfig.class)
        .equalTo("serverConfigId", serverConfigId)
        .isNotNull("hostname")
        .isNull("connectionError")
        .isNotNull("session")
        .isNotNull("token")
        .equalTo("tokenVerified", true)
        .findAll();
  }

  @Override protected void onCollectionChanged(List<ServerConfig> list) {
    int origCount = count;
    count = list.size();
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
    RealmHelperBolts.executeTransaction(realm -> {
      realm.delete(RoomSubscription.class);
      return null;
    }).onSuccessTask(_task -> methodCall.getRooms())
        .continueWith(new LogcatIfError());

  }

  @DebugLog private void onLogout() {

  }
}
