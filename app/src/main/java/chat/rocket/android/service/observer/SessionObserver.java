package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.internal.GetUsersOfRoomsProcedure;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.model.internal.MethodCall;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

/**
 * Observes user is logged into server.
 */
public class SessionObserver extends AbstractModelObserver<Session> {
  private int count;

  /**
   * constructor.
   */
  public SessionObserver(Context context, RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, realmHelper, ddpClient);
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

  }

  @DebugLog private void onLogout() {
    realmHelper.executeTransaction(realm -> {
      // remove all tables. ONLY INTERNAL TABLES!.
      realm.delete(MethodCall.class);
      realm.delete(LoadMessageProcedure.class);
      realm.delete(GetUsersOfRoomsProcedure.class);
      return null;
    }).continueWith(new LogcatIfError());
  }
}
