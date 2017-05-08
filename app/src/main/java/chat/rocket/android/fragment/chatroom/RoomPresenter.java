package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.SyncState;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Settings;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.android.service.ConnectivityManagerApi;

public class RoomPresenter extends BasePresenter<RoomContract.View>
    implements RoomContract.Presenter {

  private final String roomId;
  private final MessageInteractor messageInteractor;
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final AbsoluteUrlHelper absoluteUrlHelper;
  private final MethodCallHelper methodCallHelper;
  private final ConnectivityManagerApi connectivityManagerApi;

  public RoomPresenter(String roomId,
                       UserRepository userRepository,
                       MessageInteractor messageInteractor,
                       RoomRepository roomRepository,
                       AbsoluteUrlHelper absoluteUrlHelper,
                       MethodCallHelper methodCallHelper,
                       ConnectivityManagerApi connectivityManagerApi) {
    this.roomId = roomId;
    this.userRepository = userRepository;
    this.messageInteractor = messageInteractor;
    this.roomRepository = roomRepository;
    this.absoluteUrlHelper = absoluteUrlHelper;
    this.methodCallHelper = methodCallHelper;
    this.connectivityManagerApi = connectivityManagerApi;
  }

  @Override
  public void bindView(@NonNull RoomContract.View view) {
    super.bindView(view);

    getRoomRoles();
    getRoomInfo();
    getRoomHistoryStateInfo();
    getMessages();
    getUserPreferences();
    getAbsoluteUrl();
  }

  @Override
  public void loadMessages() {
    final Disposable subscription = getSingleRoom()
        .flatMap(messageInteractor::loadMessages)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            success -> {
              if (success) {
                connectivityManagerApi.keepAliveServer();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void loadMoreMessages() {

    final Disposable subscription = getSingleRoom()
        .flatMap(messageInteractor::loadMoreMessages)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            success -> {
              if (success) {
                connectivityManagerApi.keepAliveServer();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void onMessageSelected(@Nullable Message message) {
    if (message == null) {
      return;
    }

    if (message.getSyncState() == SyncState.FAILED) {
      view.showMessageSendFailure(message);
    }
  }

  @Override
  public void sendMessage(String messageText) {
    final Disposable subscription = getRoomUserPair()
        .flatMap(pair -> messageInteractor.send(pair.first, pair.second, messageText))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            success -> {
              if (success) {
                view.onMessageSendSuccessfully();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void resendMessage(Message message) {
    final Disposable subscription = getCurrentUser()
        .flatMap(user -> messageInteractor.resend(message, user))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    addSubscription(subscription);
  }

  @Override
  public void updateMessage(Message message, String content) {
    final Disposable subscription = getCurrentUser()
        .flatMap(user -> messageInteractor.update(message, user, content))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            success -> {
              if (success) {
                view.onMessageSendSuccessfully();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void deleteMessage(Message message) {
    final Disposable subscription = messageInteractor.delete(message)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    addSubscription(subscription);
  }

  @Override
  public void onUnreadCount() {
    final Disposable subscription = getRoomUserPair()
        .flatMap(roomUserPair -> messageInteractor
            .unreadCountFor(roomUserPair.first, roomUserPair.second))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            count -> view.showUnreadCount(count),
            Logger::report
        );

    addSubscription(subscription);
  }

  @Override
  public void onMarkAsRead() {
    final Disposable subscription = roomRepository.getById(roomId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .firstElement()
        .filter(Room::isAlert)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> methodCallHelper.readMessages(room.getRoomId())
                .continueWith(new LogIfError()),
            Logger::report
        );

    addSubscription(subscription);
  }

  private void getRoomRoles() {
    methodCallHelper.getRoomRoles(roomId);
  }

  private void getRoomInfo() {
    final Disposable subscription = roomRepository.getById(roomId)
        .distinctUntilChanged()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> view.render(room),
            Logger::report
        );

    addSubscription(subscription);
  }

  private void getRoomHistoryStateInfo() {
    final Disposable subscription = roomRepository.getHistoryStateByRoomId(roomId)
        .distinctUntilChanged()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            roomHistoryState -> {
              int syncState = roomHistoryState.getSyncState();
              view.updateHistoryState(
                  !roomHistoryState.isComplete(),
                  syncState == SyncState.SYNCED || syncState == SyncState.FAILED
              );
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  private void getMessages() {
    final Disposable subscription = roomRepository.getById(roomId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(messageInteractor::getAllFrom)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            messages -> view.showMessages(messages),
            Logger::report
        );

    addSubscription(subscription);
  }

  private void getUserPreferences() {
    final Disposable subscription = userRepository.getCurrent()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(user -> user.getSettings() != null)
        .map(User::getSettings)
        .filter(settings -> settings.getPreferences() != null)
        .map(Settings::getPreferences)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            preferences -> {
              if (preferences.isAutoImageLoad()) {
                view.autoloadImages();
              } else {
                view.manualLoadImages();
              }
            },
            Logger::report
        );

    addSubscription(subscription);
  }

  private void getAbsoluteUrl() {
    final Disposable subscription = absoluteUrlHelper.getRocketChatAbsoluteUrl()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            it -> view.setupWith(it.orNull()),
            Logger::report
        );

    addSubscription(subscription);
  }

  private Single<Pair<Room, User>> getRoomUserPair() {
    return Single.zip(
        getSingleRoom(),
        getCurrentUser(),
        Pair::new
    );
  }

  private Single<Room> getSingleRoom() {
    return roomRepository.getById(roomId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .firstElement()
        .toSingle();
  }

  private Single<User> getCurrentUser() {
    return userRepository.getCurrent()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .firstElement()
        .toSingle();
  }
}
