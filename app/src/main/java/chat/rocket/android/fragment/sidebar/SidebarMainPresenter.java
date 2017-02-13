package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.UserRepository;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SidebarMainPresenter extends BasePresenter<SidebarMainContract.View>
    implements SidebarMainContract.Presenter {

  private final String hostname;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final MethodCallHelper methodCallHelper;

  public SidebarMainPresenter(String hostname, RoomRepository roomRepository,
                              UserRepository userRepository, MethodCallHelper methodCallHelper) {
    this.hostname = hostname;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
    this.methodCallHelper = methodCallHelper;
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
    subscribeToUser();
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
  public void onLogout() {
    if (methodCallHelper != null) {
      methodCallHelper.logout().continueWith(new LogcatIfError());
    }
  }

  private void subscribeToRooms() {
    final Subscription subscription = roomRepository.getOpenRooms()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            rooms -> view.showRoomList(rooms)
        );

    addSubscription(subscription);
  }

  private void subscribeToUser() {
    final Subscription subscription = userRepository.getCurrent()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(user -> view.showUser(user));

    addSubscription(subscription);
  }

  private void updateCurrentUserStatus(String status) {
    if (methodCallHelper != null) {
      methodCallHelper.setUserStatus(status).continueWith(new LogcatIfError());
    }
  }
}
