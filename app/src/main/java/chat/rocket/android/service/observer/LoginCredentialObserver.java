package chat.rocket.android.service.observer;

import android.content.Context;
import bolts.Task;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPClientCallback;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

public class LoginCredentialObserver extends AbstractModelObserver<ServerConfig> {
  public LoginCredentialObserver(Context context, String serverConfigId,
      RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
  }

  @Override protected RealmResults<ServerConfig> queryItems(Realm realm) {
    return realm.where(ServerConfig.class)
        .equalTo("tokenVerified", false)
        .beginGroup()
        .equalTo("credential.type", "email")
        .isNotNull("credential.username")
        .isNotNull("credential.hashedPasswd")
        .or()
        .notEqualTo("credential.type", "email")
        .isNotNull("credential.credentialToken")
        .isNotNull("credential.credentialSecret")
        .endGroup()
        .findAll();
  }

  @Override protected void onCollectionChanged(List<ServerConfig> list) {
    if (list.isEmpty()) {
      return;
    }

    ServerConfig config = list.get(0);
    final String serverConfigId = config.getId();

    login(config).onSuccessTask(task -> {
      final String token = task.getResult().result.getString("token");
      return RealmHelperBolts.executeTransaction(realm ->
          realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
              .put("id", serverConfigId)
              .put("token", token)
              .put("tokenVerified", true)));
    }).continueWith(task -> {
      if (task.isFaulted()) {
        RealmHelperBolts.executeTransaction(realm -> {
          ServerConfig _config = realm.where(ServerConfig.class)
              .equalTo("id", serverConfigId)
              .findFirst();

          if (_config != null) {
            _config.getCredential().deleteFromRealm();
            _config.setToken(null);
          }
          return null;
        }).continueWith(new LogcatIfError());
      }
      return null;
    });
  }

  private Task<DDPClientCallback.RPC> login(ServerConfig config) {
    if (!TextUtils.isEmpty(config.getToken())) {
      return webSocketAPI.loginWithToken(config.getToken());
    }

    return webSocketAPI.login(config.getCredential());
  }
}
