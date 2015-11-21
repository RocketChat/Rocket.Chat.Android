package chat.rocket.android;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;

import chat.rocket.android.api.OkHttpHelper;
import ollie.Ollie;

public class RocketChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Ollie.with(this)
                .setName("rockets.db")
                .setVersion(1)
                .setLogLevel(Ollie.LogLevel.BASIC)
                .init();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
        OkHttpHelper.getClient()
                .networkInterceptors().add(new StethoInterceptor());
    }
}
