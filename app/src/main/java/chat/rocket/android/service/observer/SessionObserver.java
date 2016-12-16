package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.internal.GetUsersOfRoomsProcedure;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.model.internal.MethodCall;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.internal.StreamRoomMessageManager;
import hugo.weaving.DebugLog;

/**
 * Observes user is logged into server.
 */
public class SessionObserver extends AbstractModelObserver<Session> {
  private final StreamRoomMessageManager streamNotifyMessage;
  private int count;

  /**
   * constructor.
   */
  public SessionObserver(Context context, String hostname,
                         RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    count = 0;

    streamNotifyMessage = new StreamRoomMessageManager(context, hostname, realmHelper, ddpClient);
  }

  @Override
  public RealmResults<Session> queryItems(Realm realm) {
    return realm.where(Session.class)
        .isNotNull("token")
        .equalTo("tokenVerified", true)
        .isNull("error")
        .findAll();
  }

  @Override
  public void onUpdateResults(List<Session> results) {
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

  @DebugLog
  private void onLogin() {
    streamNotifyMessage.register();
    new MethodCallHelper(realmHelper, ddpClient).getPublicSettings()
        .continueWith(new LogcatIfError());
  }

  @DebugLog
  private void onLogout() {
    streamNotifyMessage.unregister();

    realmHelper.executeTransaction(realm -> {
      // remove all tables. ONLY INTERNAL TABLES!.
      realm.delete(PublicSetting.class);
      realm.delete(MethodCall.class);
      realm.delete(LoadMessageProcedure.class);
      realm.delete(GetUsersOfRoomsProcedure.class);
      return null;
    }).continueWith(new LogcatIfError());
  }
}
