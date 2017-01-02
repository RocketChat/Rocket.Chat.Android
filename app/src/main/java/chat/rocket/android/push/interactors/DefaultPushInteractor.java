package chat.rocket.android.push.interactors;

import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmStore;

public class DefaultPushInteractor implements PushInteractor {

  @Override
  public String getServerConfigId(String hostname) {
    final ServerConfig serverConfig = RealmStore.getDefault()
        .executeTransactionForRead(
            realm -> realm.where(ServerConfig.class)
                .equalTo(ServerConfig.HOSTNAME, ServerPolicyHelper.enforceHostname(hostname))
                .findFirst());

    return serverConfig != null ? serverConfig.getServerConfigId() : "";
  }
}
