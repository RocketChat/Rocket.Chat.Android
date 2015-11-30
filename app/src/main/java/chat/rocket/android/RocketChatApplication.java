package chat.rocket.android;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import chat.rocket.android.api.OkHttpHelper;

public class RocketChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        OkHttpHelper.getClient()
                .networkInterceptors().add(new StethoInterceptor());

        Picasso picasso = new Picasso.Builder(this).downloader(new OkHttpDownloader(OkHttpHelper.getClient())).build();
        Picasso.setSingletonInstance(picasso);
    }
}
