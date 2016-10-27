package chat.rocket.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import chat.rocket.android.api.OkHttpHelper;
import io.fabric.sdk.android.Fabric;

public class RocketChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        Picasso picasso = new Picasso.Builder(this).downloader(new OkHttp3Downloader(OkHttpHelper.getClient())).build();
        Picasso.setSingletonInstance(picasso);

        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
        RealmConfig.setDefault(this);
    }
}
