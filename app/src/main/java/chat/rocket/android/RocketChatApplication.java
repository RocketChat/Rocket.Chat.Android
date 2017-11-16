package chat.rocket.android;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.concurrent.TimeoutException;

import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.RocketChatPersistenceRealm;
import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

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
        DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());
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

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof TimeoutException) {
                // Some work timed-out after a server change is most probable.
                return;
            }

            Thread.currentThread().getUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), e);
        });

        instance = this;
    }
}