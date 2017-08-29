package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import chat.rocket.core.models.Spotlight;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import java.util.List;

public class SidebarMainPresenter extends BasePresenter<SidebarMainContract.View> implements SidebarMainContract.Presenter {
  private final String hostname;
  private String userId;
  private final RoomInteractor roomInteractor;
  private final UserRepository userRepository;
  private final RocketChatCache rocketChatCache;
  private final AbsoluteUrlHelper absoluteUrlHelper;
  private final MethodCallHelper methodCallHelper;
  private RealmSpotlightRepository realmSpotlightRepository;

  public SidebarMainPresenter(String hostname,
                              RoomInteractor roomInteractor,
                              UserRepository userRepository,
                              RocketChatCache rocketChatCache,
                              AbsoluteUrlHelper absoluteUrlHelper,
                              MethodCallHelper methodCallHelper,
                              RealmSpotlightRepository realmSpotlightRepository) {
    this.hostname = hostname;
    this.roomInteractor = roomInteractor;
    this.userRepository = userRepository;
    this.rocketChatCache = rocketChatCache;
    this.absoluteUrlHelper = absoluteUrlHelper;
    this.methodCallHelper = methodCallHelper;
    this.realmSpotlightRepository = realmSpotlightRepository;
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
        .subscribe(
          pair -> {
            userId = pair.first.orNull().getId();
            view.show(pair.first.orNull());
          },
          Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void onRoomSelected(Room room) {
    rocketChatCache.setSelectedRoomId(room.getRoomId());
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
  public void onLogout() {
      methodCallHelper.logout().continueWith(new LogIfError());
  }

  private void subscribeToRooms() {
    final Disposable subscription = roomInteractor.getOpenRooms()
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::processRooms, Logger::report);
    addSubscription(subscription);
  }

  private void processRooms(List<Room> roomList) {
    view.showRoomList(roomList);
    for (Room room: roomList) {
      if (room.isDirectMessage()) {
        subscribeToDirectMessages(room.getName());
      }
    }
  }

  private void subscribeToDirectMessages(String username) {
    final Disposable disposable = userRepository.getByUsername(username)
        .distinctUntilChanged()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(view::showUserStatus, Logger::report);
    addSubscription(disposable);
  }

  private void updateCurrentUserStatus(String status) {
      methodCallHelper.setUserStatus(status).continueWith(new LogIfError());
  }
}