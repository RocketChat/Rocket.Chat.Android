package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.api.DDPClientWraper;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

public class TokenLoginObserver extends AbstractModelObserver<Session> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
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
    methodCall.loginWithToken(session.getToken()).continueWith(new LogcatIfError());
  }
}
