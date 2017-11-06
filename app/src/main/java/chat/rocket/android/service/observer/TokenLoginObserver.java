package chat.rocket.android.service.observer;

import android.content.Context;

import java.util.List;

import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import io.realm.Realm;
import io.realm.RealmResults;

public class TokenLoginObserver extends AbstractModelObserver<RealmSession> {

  private final MethodCallHelper methodCall;

  public TokenLoginObserver(Context context, String hostname,
                            RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    methodCall = new MethodCallHelper(realmHelper);
  }

  @Override
  public RealmResults<RealmSession> queryItems(Realm realm) {
    return realm.where(RealmSession.class)
        .isNotNull(RealmSession.TOKEN)
        .equalTo(RealmSession.TOKEN_VERIFIED, false)
        .isNull(RealmSession.ERROR)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<RealmSession> results) {
    if (results.isEmpty()) {
      return;
    }

    RealmSession session = results.get(0);
    methodCall.loginWithToken(session.getToken()).continueWith(new LogIfError());
  }
}
