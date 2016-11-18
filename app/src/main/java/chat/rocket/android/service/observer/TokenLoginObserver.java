package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.api.RocketChatWebSocketAPI;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

public class TokenLoginObserver extends AbstractModelObserver<Session> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, RealmHelper realmHelper, RocketChatWebSocketAPI api) {
    super(context, realmHelper, api);
    methodCall = new MethodCallHelper(realmHelper, api);
  }

  @Override public RealmResults<Session> queryItems(Realm realm) {
    return realm.where(Session.class)
        .isNotNull("token")
        .equalTo("tokenVerified", false)
        .isNull("error")
        .findAll();
  }

  @Override public void onUpdateResults(List<Session> results) {
    if (results.isEmpty()) {
      return;
    }

    Session session = results.get(0);
    methodCall.loginWithToken(session.getToken())
        .continueWith(task -> {
          if (task.isFaulted()) {
            Session.logError(realmHelper, task.getError());
          }
          return null;
        });
  }
}
