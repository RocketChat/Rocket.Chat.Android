package chat.rocket.android;

import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.List;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android.wrappers.InstabugWrapper;
import chat.rocket.persistence.realm.RocketChatPersistenceRealm;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {
  @Override
  public void onCreate() {
    super.onCreate();

    RocketChatPersistenceRealm.init(this);

    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmStore.put(serverInfo.getHostname());
    }

    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
        .build());

    InstabugWrapper.build(this, getString(R.string.instabug_api_key));

    //TODO: add periodic trigger for RocketChatService.keepAlive(this) here!

    RocketChatWidgets.initialize(this, OkHttpHelper.getClientForDownloadFile(this));
  }
}
