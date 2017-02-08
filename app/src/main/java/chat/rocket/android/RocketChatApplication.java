package chat.rocket.android;

import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import java.util.List;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.ServerInfo;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android.wrappers.InstabugWrapper;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {
  @Override
  public void onCreate() {
    super.onCreate();

    Realm.init(this);
    Realm.setDefaultConfiguration(
        new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build());

    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmStore.put(serverInfo.hostname);
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
