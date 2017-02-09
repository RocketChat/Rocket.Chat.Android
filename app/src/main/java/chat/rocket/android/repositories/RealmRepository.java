package chat.rocket.android.repositories;

import android.os.Handler;
import android.os.Looper;
import io.realm.Realm;

public class RealmRepository {

  protected void close(Realm realm, Looper looper) {
    new Handler(looper).post(new Runnable() {
      @Override
      public void run() {
        realm.close();
      }
    });
  }
}
