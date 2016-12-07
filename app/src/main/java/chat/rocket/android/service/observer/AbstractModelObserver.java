package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.api.DDPClientWraper;
import io.realm.RealmObject;

abstract class AbstractModelObserver<T extends RealmObject>
    implements Registerable, RealmListObserver.Query<T>, RealmListObserver.OnUpdateListener<T> {

  protected final Context context;
  protected final RealmHelper realmHelper;
  protected final DDPClientWraper ddpClient;
  private final RealmListObserver observer;

  protected AbstractModelObserver(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    this.context = context;
    this.realmHelper = realmHelper;
    this.ddpClient = ddpClient;
    observer = realmHelper.createListObserver(this).setOnUpdateListener(this);
  }

  @Override public void register() {
    observer.sub();
  }

  @Override public void unregister() {
    observer.unsub();
  }
}
