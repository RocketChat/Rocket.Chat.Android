package chat.rocket.persistence.realm.repositories;

import android.os.Handler;
import android.os.Looper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import java.util.List;

public class RealmRepository {

  protected void close(Realm realm, Looper looper) {
    if (realm == null || looper == null) {
      return;
    }
    new Handler(looper).post(realm::close);
  }

  <T extends RealmObject> List<T> safeSubList(RealmResults<T> realmObjects,
                                              int fromIndex,
                                              int toIndex) {
    return realmObjects.subList(Math.max(0, fromIndex), Math.min(realmObjects.size(), toIndex));
  }
}
