package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;

public class SidebarMainPresenter extends BasePresenter<SidebarMainContract.View>
    implements SidebarMainContract.Presenter {

  private final String hostname;
  private final RoomInteractor roomInteractor;
  private final UserRepository userRepository;
  private final RocketChatCache rocketChatCache;
  private final AbsoluteUrlHelper absoluteUrlHelper;
  private final MethodCallHelper methodCallHelper;

  public SidebarMainPresenter(String hostname, RoomInteractor roomInteractor,
                              UserRepository userRepository,
                              RocketChatCache rocketChatCache,
                              AbsoluteUrlHelper absoluteUrlHelper,
                              MethodCallHelper methodCallHelper) {
    this.hostname = hostname;
    this.roomInteractor = roomInteractor;
    this.userRepository = userRepository;
    this.rocketChatCache = rocketChatCache;
    this.absoluteUrlHelper = absoluteUrlHelper;
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

    final Disposable subscription = Flowable.combineLatest(
        userRepository.getCurrent().distinctUntilChanged(),
        absoluteUrlHelper.getRocketChatAbsoluteUrl().toFlowable(),
        Pair::new
    )
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> view.show(pair.first.orNull(), pair.second.orNull()));

    addSubscription(subscription);
  }

  @Override
  public void onRoomSelected(Room room) {
    rocketChatCache.setSelectedRoomId(room.getRoomId());
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
      methodCallHelper.logout().continueWith(new LogIfError());
    }
  }

  private void subscribeToRooms() {
    final Disposable subscription = roomInteractor.getOpenRooms()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            rooms -> view.showRoomList(rooms)
        );

    addSubscription(subscription);
  }

  private void updateCurrentUserStatus(String status) {
    if (methodCallHelper != null) {
      methodCallHelper.setUserStatus(status).continueWith(new LogIfError());
    }
  }
}
