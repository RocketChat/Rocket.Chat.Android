package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;

public class TokenLoginObserver extends AbstractModelObserver<Session> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, String hostname,
                            RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
  }

  @Override
  public RealmResults<Session> queryItems(Realm realm) {
    return realm.where(Session.class)
        .isNotNull(Session.TOKEN)
        .equalTo(Session.TOKEN_VERIFIED, false)
        .isNull(Session.ERROR)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<Session> results) {
    if (results.isEmpty()) {
      return;
    }

    Session session = results.get(0);
    methodCall.loginWithToken(session.getToken()).continueWith(new LogcatIfError());
  }
}
