package chat.rocket.android;

import android.os.StrictMode;

import com.facebook.stetho.Stetho;
import com.tspoon.traceur.Traceur;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

public class RocketChatApplicationDebug extends RocketChatApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    enableStrictMode();
    enableStetho();
    enableTraceur();
  }

  private void enableTraceur() {
    Traceur.enableLogging();
  }

  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .build());
  }

  private void enableStetho() {
    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).withLimit(Long.MAX_VALUE).build())
        .build());
  }
}
