package chat.rocket.android.service.observer;

import android.content.Context;

import chat.rocket.android.service.Registrable;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmListObserver;
import io.realm.RealmObject;

abstract class AbstractModelObserver<T extends RealmObject>
    implements Registrable, RealmListObserver.Query<T>, RealmListObserver.OnUpdateListener<T> {

  protected final Context context;
  protected final String hostname;
  protected final RealmHelper realmHelper;
  private final RealmListObserver observer;

  protected AbstractModelObserver(Context context, String hostname,
                                  RealmHelper realmHelper) {
    this.context = context;
    this.hostname = hostname;
    this.realmHelper = realmHelper;
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
