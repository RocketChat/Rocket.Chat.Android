package chat.rocket.android.activity;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.service.ServerConnectivity;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Session;
import chat.rocket.core.models.User;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
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

  public MainPresenter(RoomInteractor roomInteractor,
                       CanCreateRoomInteractor canCreateRoomInteractor,
                       SessionInteractor sessionInteractor,
                       MethodCallHelper methodCallHelper,
                       ConnectivityManagerApi connectivityManagerApi,
                       RocketChatCache rocketChatCache) {
    this.roomInteractor = roomInteractor;
    this.canCreateRoomInteractor = canCreateRoomInteractor;
    this.sessionInteractor = sessionInteractor;
    this.methodCallHelper = methodCallHelper;
    this.connectivityManagerApi = connectivityManagerApi;
    this.rocketChatCache = rocketChatCache;
  }

  @Override
  public void bindViewOnly(@NonNull MainContract.View view) {
    super.bindView(view);
    subscribeToUnreadCount();
    subscribeToSession();
    setUserOnline();
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
    view.showConnecting();
    connectivityManagerApi.keepAliveServer();
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
    Disposable disposable = RxJavaInterop.toV2Flowable(connectivityManagerApi.getServerConnectivityAsObservable())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    connectivity -> {
                      if (connectivity.state == ServerConnectivity.STATE_CONNECTED) {
                        view.showConnectionOk();
                        return;
                      }
                      view.showConnecting();
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
