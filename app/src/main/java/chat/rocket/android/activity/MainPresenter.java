package chat.rocket.android.activity;

import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.service.ServerConnectivity;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.models.Session;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.core.utils.Pair;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainPresenter extends BasePresenter<MainContract.View>
        implements MainContract.Presenter {

    private final CanCreateRoomInteractor canCreateRoomInteractor;
    private final RoomInteractor roomInteractor;
    private final SessionInteractor sessionInteractor;
    private final MethodCallHelper methodCallHelper;
    private final ConnectivityManagerApi connectivityManagerApi;
    private final RocketChatCache rocketChatCache;
    private final PublicSettingRepository publicSettingRepository;

    public MainPresenter(RoomInteractor roomInteractor,
                         CanCreateRoomInteractor canCreateRoomInteractor,
                         SessionInteractor sessionInteractor,
                         MethodCallHelper methodCallHelper,
                         ConnectivityManagerApi connectivityManagerApi,
                         RocketChatCache rocketChatCache, PublicSettingRepository publicSettingRepository) {
        this.roomInteractor = roomInteractor;
        this.canCreateRoomInteractor = canCreateRoomInteractor;
        this.sessionInteractor = sessionInteractor;
        this.methodCallHelper = methodCallHelper;
        this.connectivityManagerApi = connectivityManagerApi;
        this.rocketChatCache = rocketChatCache;
        this.publicSettingRepository = publicSettingRepository;
    }

    @Override
    public void bindViewOnly(@NonNull MainContract.View view) {
        super.bindView(view);
        subscribeToUnreadCount();
        subscribeToSession();
        setUserOnline();
    }

    @Override
    public void loadSignedInServers(@NonNull String hostname) {
        final Disposable disposable = publicSettingRepository.getById(PublicSettingsConstants.Assets.LOGO)
                .zipWith(publicSettingRepository.getById(PublicSettingsConstants.General.SITE_NAME), Pair::new)
                .map(this::getLogoAndSiteNamePair)
                .map(settings -> getServerList(hostname, settings))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showSignedInServers,
                        RCLog::e
                );

        addSubscription(disposable);
    }

    @Override
    public void bindView(@NonNull MainContract.View view) {
        super.bindView(view);

        if (shouldLaunchAddServerActivity()) {
            view.showAddServerScreen();
            return;
        }

        openRoom();

        subscribeToNetworkChanges();
        subscribeToUnreadCount();
        subscribeToSession();
        setUserOnline();
    }

    @Override
    public void release() {
        setUserAway();

        super.release();
    }

    @Override
    public void onOpenRoom(String hostname, String roomId) {
        final Disposable subscription = canCreateRoomInteractor.canCreate(roomId)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allowed -> {
                            if (allowed) {
                                view.showRoom(hostname, roomId);
                            } else {
                                view.showHome();
                            }
                        },
                        Logger::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onRetryLogin() {
        final Disposable subscription = sessionInteractor.retryLogin()
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void beforeLogoutCleanUp() {
        clearSubscriptions();
    }

    private Pair<String, String> getLogoAndSiteNamePair(Pair<Optional<PublicSetting>, Optional<PublicSetting>> settingsPair) {
        String logoUrl = "";
        String siteName = "";
        if (settingsPair.first.isPresent()) {
            logoUrl = settingsPair.first.get().getValue();
        }
        if (settingsPair.second.isPresent()) {
            siteName = settingsPair.second.get().getValue();
        }
        return new Pair<>(logoUrl, siteName);
    }

    private List<Pair<String, Pair<String, String>>> getServerList(String hostname, Pair<String, String> serverInfoPair) throws JSONException {
        JSONObject jsonObject = new JSONObject(serverInfoPair.first);
        String logoUrl = (jsonObject.has("url")) ?
                jsonObject.optString("url") : jsonObject.optString("defaultUrl");
        String siteName = serverInfoPair.second;
        rocketChatCache.addHostname(hostname.toLowerCase(), logoUrl, siteName);
        return rocketChatCache.getServerList();
    }

    private void openRoom() {
        String hostname = rocketChatCache.getSelectedServerHostname();
        String roomId = rocketChatCache.getSelectedRoomId();

        if (roomId == null || roomId.length() == 0) {
            view.showHome();
            return;
        }

        onOpenRoom(hostname, roomId);
    }

    private void subscribeToUnreadCount() {
        final Disposable subscription = Flowable.combineLatest(
                roomInteractor.getTotalUnreadRoomsCount(),
                roomInteractor.getTotalUnreadMentionsCount(),
                (Pair::new)
        )
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> view.showUnreadCount(pair.first, pair.second),
                        Logger::report
                );

        addSubscription(subscription);
    }

    private void subscribeToSession() {
        final Disposable subscription = sessionInteractor.getDefault()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sessionOptional -> {
                            Session session = sessionOptional.orNull();
                            if (session == null || session.getToken() == null) {
                                view.showLoginScreen();
                                return;
                            }

                            String error = session.getError();
                            if (error != null && error.length() != 0) {
                                view.showConnectionError();
                                return;
                            }

                            if (!session.isTokenVerified()) {
                                view.showConnecting();
                                return;
                            }

                            view.showConnectionOk();
                        },
                        Logger::report
                );

        addSubscription(subscription);
    }

    private void subscribeToNetworkChanges() {
        Disposable disposable = connectivityManagerApi.getServerConnectivityAsObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        connectivity -> {
                            if (connectivity.state == ServerConnectivity.STATE_CONNECTED) {
                                view.showConnectionOk();
                                view.refreshRoom();
                            } else if (connectivity.state == ServerConnectivity.STATE_DISCONNECTED) {
                                if (connectivity.code == DDPClient.REASON_NETWORK_ERROR) {
                                    view.showConnectionError();
                                }
                            } else {
                                view.showConnecting();
                            }
                        },
                        Logger::report
                );

        addSubscription(disposable);
    }

    private void setUserOnline() {
        methodCallHelper.setUserPresence(User.STATUS_ONLINE)
                .continueWith(new LogIfError());
    }

    private void setUserAway() {
        methodCallHelper.setUserPresence(User.STATUS_AWAY)
                .continueWith(new LogIfError());
    }

    private boolean shouldLaunchAddServerActivity() {
        return connectivityManagerApi.getServerList().isEmpty();
    }
}
