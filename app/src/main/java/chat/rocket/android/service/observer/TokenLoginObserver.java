package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.MethodCallHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

public class TokenLoginObserver extends AbstractModelObserver<ServerConfig> {

  public TokenLoginObserver(Context context, String serverConfigId, RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
  }

  @Override protected RealmResults<ServerConfig> queryItems(Realm realm) {
    return realm.where(ServerConfig.class)
        .isNotNull("token")
        .equalTo("tokenVerified", false)
        .findAll();
  }

  @Override protected void onCollectionChanged(List<ServerConfig> list) {
    if (list.isEmpty()) {
      return;
    }

    ServerConfig config = list.get(0);
    new MethodCallHelper(serverConfigId, webSocketAPI).loginWithToken(config.getToken())
        .continueWith(new LogcatIfError());
  }
}
