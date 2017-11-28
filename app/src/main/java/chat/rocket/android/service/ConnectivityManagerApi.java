package chat.rocket.android.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import chat.rocket.core.models.ServerInfo;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * interfaces used for Activity/Fragment and other UI-related logic.
 */
public interface ConnectivityManagerApi {
    void keepAliveServer();

    void addOrUpdateServer(String hostname, @Nullable String name, boolean insecure);

    void removeServer(String hostname);

    Single<Boolean> connect(String hostname);

    List<ServerInfo> getServerList();

    Flowable<ServerConnectivity> getServerConnectivityAsObservable();

    int getConnectivityState(@NonNull String hostname);

    void resetConnectivityStateList();
}
