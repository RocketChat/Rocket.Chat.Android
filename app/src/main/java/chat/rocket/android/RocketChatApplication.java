package chat.rocket.android;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import chat.rocket.android.helper.OkHttpHelper;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import java.util.List;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.persistence.realm.RocketChatPersistenceRealm;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {

  private static RocketChatApplication instance;

  public static RocketChatApplication getInstance() {
    return instance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());

    RocketChatPersistenceRealm.init(this);

    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(this).getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmStore.put(serverInfo.getHostname());
    }

    RocketChatWidgets.initialize(this, OkHttpHelper.INSTANCE.getClientForDownloadFile(this));

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    instance = this;
  }
}