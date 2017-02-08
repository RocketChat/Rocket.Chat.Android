package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.RealmObject;

import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmListObserver;
import chat.rocket.android.service.DDPClientRef;
import chat.rocket.android.service.Registrable;

abstract class AbstractModelObserver<T extends RealmObject>
    implements Registrable, RealmListObserver.Query<T>, RealmListObserver.OnUpdateListener<T> {

  protected final Context context;
  protected final String hostname;
  protected final RealmHelper realmHelper;
  protected final DDPClientRef ddpClientRef;
  private final RealmListObserver observer;

  protected AbstractModelObserver(Context context, String hostname,
                                  RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    this.context = context;
    this.hostname = hostname;
    this.realmHelper = realmHelper;
    this.ddpClientRef = ddpClientRef;
    observer = realmHelper.createListObserver(this).setOnUpdateListener(this);
  }

  @Override
  public void register() {
    observer.sub();
  }

  @Override
  public void unregister() {
    observer.unsub();
  }
}
