package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.RealmObject;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;

abstract class AbstractModelObserver<T extends RealmObject> extends RealmListObserver<T>
    implements Registerable {

  protected final Context mContext;
  protected final RocketChatWebSocketAPI mAPI;

  protected AbstractModelObserver(Context context, RocketChatWebSocketAPI api) {
    mContext = context;
    mAPI = api;
  }

  @Override public void register() {
    sub();
  }

  @Override public void unregister() {
    unsub();
  }
}
