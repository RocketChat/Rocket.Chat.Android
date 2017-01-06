package chat.rocket.android.service.observer;

import android.content.Context;
import android.content.Intent;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;
import chat.rocket.android.push.gcm.GcmRegistrationIntentService;
import chat.rocket.android.push.interactors.DefaultPushInteractor;
import chat.rocket.android.push.interactors.PushInteractor;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class PushSettingsObserver extends AbstractModelObserver<PublicSetting> {

  public PushSettingsObserver(Context context, String hostname,
                              RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override
  public void onUpdateResults(List<PublicSetting> results) {
    RealmHelper realmHelper = RealmStore.getDefault();

    PushInteractor interactor = new DefaultPushInteractor();
    String serverConfigId = interactor.getServerConfigId(hostname);

    final ServerConfig serverConfig = realmHelper.executeTransactionForRead(
        realm -> realm.where(ServerConfig.class).equalTo(ServerConfig.ID, serverConfigId)
            .findFirst());

    serverConfig.setSyncPushToken(isPushEnabled(results));

    realmHelper
        .executeTransaction(realm -> realm.copyToRealmOrUpdate(serverConfig))
        .continueWith(task -> {
          if (serverConfig.shouldSyncPushToken()) {
            Intent intent = new Intent(
                context.getApplicationContext(), GcmRegistrationIntentService.class);
            context.getApplicationContext().startService(intent);
          }

          return task;
        })
        .continueWith(new LogcatIfError());
  }

  @Override
  public RealmResults<PublicSetting> queryItems(Realm realm) {
    return realm.where(PublicSetting.class)
        .equalTo(PublicSetting.ID, PublicSettingsConstants.Push.ENABLE)
        .or()
        .equalTo(PublicSetting.ID, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER)
        .findAll();
  }

  private boolean isPushEnabled(List<PublicSetting> results) {
    return isPushEnable(results) && isGcmValid(results);
  }

  private boolean isPushEnable(List<PublicSetting> results) {
    for (PublicSetting setting : results) {
      if (PublicSettingsConstants.Push.ENABLE.equals(setting.getId())) {
        return "true".equals(setting.getValue());
      }
    }
    return false;
  }

  private boolean isGcmValid(List<PublicSetting> results) {
    for (PublicSetting setting : results) {
      if (PublicSettingsConstants.Push.GCM_PROJECT_NUMBER.equals(setting.getId())) {
        return setting.getValue() != null && !"".equals(setting.getValue());
      }
    }
    return false;
  }
}
