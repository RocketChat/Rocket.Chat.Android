package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.webkit.CookieManager;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CheckUpdateInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.SpotlightRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SidebarMainPresenter extends BasePresenter<SidebarMainContract.View> implements SidebarMainContract.Presenter {
    private final String hostname;
    private final RoomInteractor roomInteractor;
    private final UserRepository userRepository;
    private final RocketChatCache rocketChatCache;
    private final AbsoluteUrlHelper absoluteUrlHelper;
    private final MethodCallHelper methodCallHelper;
    private final SpotlightRepository realmSpotlightRepository;
    private final CheckUpdateInteractor updateInteractor;
    private List<RoomSidebar> roomSidebarList;

    public SidebarMainPresenter(String hostname,
                                RoomInteractor roomInteractor,
                                UserRepository userRepository,
                                RocketChatCache rocketChatCache,
                                AbsoluteUrlHelper absoluteUrlHelper,
                                MethodCallHelper methodCallHelper,
                                RealmSpotlightRepository realmSpotlightRepository,
                                CheckUpdateInteractor updateInteractor) {
        this.hostname = hostname;
        this.roomInteractor = roomInteractor;
        this.userRepository = userRepository;
        this.rocketChatCache = rocketChatCache;
        this.absoluteUrlHelper = absoluteUrlHelper;
        this.methodCallHelper = methodCallHelper;
        this.realmSpotlightRepository = realmSpotlightRepository;
        this.updateInteractor = updateInteractor;
    }

    @Override
    public void bindView(@NonNull SidebarMainContract.View view) {
        super.bindView(view);

        if (TextUtils.isEmpty(hostname)) {
            view.showEmptyScreen();
            return;
        }

        view.showScreen();

        subscribeToRooms();

        subscribeToUpdates();

        final Disposable subscription = Flowable.combineLatest(
                userRepository.getCurrent().distinctUntilChanged(),
                absoluteUrlHelper.getRocketChatAbsoluteUrl().toFlowable(),
                Pair::new
        )
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> view.show(pair.first.orNull()), Logger::report);

        addSubscription(subscription);
    }

    @Override
    public void onRoomSelected(RoomSidebar roomSidebar) {
        rocketChatCache.setSelectedRoomId(roomSidebar.getRoomId());
    }

    @Override
    public Flowable<List<Spotlight>> searchSpotlight(String term) {
        methodCallHelper.searchSpotlight(term);
        return realmSpotlightRepository.getSuggestionsFor(term, 10);
    }

    @Override
    public void onSpotlightSelected(Spotlight spotlight) {
        if (spotlight.getType().equals(Room.TYPE_DIRECT_MESSAGE)) {
            String username = spotlight.getName();
            methodCallHelper.createDirectMessage(username)
                    .continueWithTask(task -> {
                        if (task.isCompleted()) {
                            rocketChatCache.setSelectedRoomId(task.getResult());
                        }
                        return null;
                    });
        } else {
            methodCallHelper.joinRoom(spotlight.getId())
                    .continueWithTask(task -> {
                        if (task.isCompleted()) {
                            rocketChatCache.setSelectedRoomId(spotlight.getId());
                        }
                        return null;
                    });
        }
    }

    @Override
    public void onUserOnline() {
        updateCurrentUserStatus(User.STATUS_ONLINE);
    }

    @Override
    public void onUserAway() {
        updateCurrentUserStatus(User.STATUS_AWAY);
    }

    @Override
    public void onUserBusy() {
        updateCurrentUserStatus(User.STATUS_BUSY);
    }

    @Override
    public void onUserOffline() {
        updateCurrentUserStatus(User.STATUS_OFFLINE);
    }

    @Override
    public void onLogout(Continuation<Void, Object> continuation) {
        methodCallHelper.logout().continueWith(task -> {
            if (task.isFaulted()) {
                Logger.report(task.getError());
                return Task.forError(task.getError());
            }
            return task.onSuccess(continuation);
        });
    }

    @Override
    public void beforeLogoutCleanUp() {
        clearSubscriptions();
        String currentHostname = rocketChatCache.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(currentHostname);
        realmHelper.executeTransaction(realm -> {
            realm.executeTransactionAsync(realmObj -> realmObj.deleteAll());
            CookieManager.getInstance().removeAllCookie();
            ConnectivityManagerApi connectivityManagerApi = ConnectivityManager.getInstance(RocketChatApplication.getInstance());
            connectivityManagerApi.removeServer(currentHostname);
            rocketChatCache.removeHostname(currentHostname);
            rocketChatCache.removeSelectedRoomId(currentHostname);
            rocketChatCache.setSelectedServerHostname(rocketChatCache.getFirstLoggedHostnameIfAny());
            view.onLogoutCleanUp();
            return null;
        });
    }

    @Override
    public void disposeSubscriptions() {
        clearSubscriptions();
    }

    private void subscribeToRooms() {
        final Disposable subscription = roomInteractor.getOpenRooms()
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processRooms, Logger::report);
        addSubscription(subscription);
    }

    private void subscribeToUpdates() {
        Disposable subscription = updateInteractor.check()
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processUpdate, Logger::report);
        addSubscription(subscription);
    }

    private void processRooms(List<Room> roomList) {
        roomSidebarList = new ArrayList<>();
        List<String> userToObserverList = new ArrayList<>();

        for (Room room : roomList) {
            String roomName = room.getName();
            String roomType = room.getType();

            RoomSidebar roomSidebar = new RoomSidebar();

            roomSidebar.setId(room.getId());
            roomSidebar.setRoomId(room.getRoomId());
            roomSidebar.setRoomName(roomName);
            roomSidebar.setType(roomType);
            roomSidebar.setAlert(room.isAlert());
            roomSidebar.setFavorite(room.isFavorite());
            roomSidebar.setUnread(room.getUnread());
            roomSidebar.setUpdateAt(room.getUpdatedAt());
            roomSidebar.setLastSeen(room.getLastSeen());

            if (roomType.equals(Room.TYPE_DIRECT_MESSAGE)) {
                userToObserverList.add(roomName);
            }

            roomSidebarList.add(roomSidebar);
        }
        if (userToObserverList.isEmpty()) {
            view.showRoomSidebarList(roomSidebarList);
        } else {
            getUsersStatus();
        }
    }

    private void processUpdate(Boolean updateAvailable) {
        if (updateAvailable) {
            view.showUpdateAvailable();
        } else {
            view.showNoUpdateAvailable();
        }
    }

    private void getUsersStatus() {
        // TODO Filter when Android Studion uses the java8 features (removeIf).
        // .filter(userList -> userList.removeIf(user -> !userToObserverList.contains(user.getUsername())))
        final Disposable subscription = userRepository.getAll()
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processUsers, Logger::report);
        addSubscription(subscription);
  }

    private void processUsers(List<User> userList) {
        for (User user: userList) {
            for(RoomSidebar roomSidebar: roomSidebarList) {
                if (roomSidebar.getRoomName().equals(user.getUsername())) {
                    roomSidebar.setUserStatus(user.getStatus());
                }
            }
        }
        view.showRoomSidebarList(roomSidebarList);
    }

    private void updateCurrentUserStatus(String status) {
        methodCallHelper.setUserStatus(status).continueWith(new LogIfError());
    }
}