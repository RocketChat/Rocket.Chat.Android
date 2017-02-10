package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.persistence.realm.models.internal.Session;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.service.DDPClientRef;

public class TokenLoginObserver extends AbstractModelObserver<Session> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, String hostname,
                            RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
    methodCall = new MethodCallHelper(realmHelper, ddpClientRef);
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
