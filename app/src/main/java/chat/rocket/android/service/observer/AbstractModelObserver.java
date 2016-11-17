package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.RealmObject;

abstract class AbstractModelObserver<T extends RealmObject>
    implements Registerable, RealmListObserver.Query<T>, RealmListObserver.OnUpdateListener<T> {

  protected final Context context;
  protected final RealmHelper realmHelper;
  protected final RocketChatWebSocketAPI webSocketAPI;
  private final RealmListObserver observer;

  protected AbstractModelObserver(Context context, RealmHelper realmHelper,
      RocketChatWebSocketAPI api) {
    this.context = context;
    this.realmHelper = realmHelper;
    webSocketAPI = api;
    observer = realmHelper.createListObserver(this).setOnUpdateListener(this);
  }

  @Override public void register() {
    observer.sub();
  }

  @Override public void keepalive() {
    observer.keepalive();
  }

  @Override public void unregister() {
    observer.unsub();
  }
}
