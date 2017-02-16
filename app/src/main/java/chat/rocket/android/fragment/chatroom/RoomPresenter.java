package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.SyncState;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.android.service.ConnectivityManagerApi;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class RoomPresenter extends BasePresenter<RoomContract.View>
    implements RoomContract.Presenter {

  private final String roomId;
  private final MessageInteractor messageInteractor;
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final MethodCallHelper methodCallHelper;
  private final ConnectivityManagerApi connectivityManagerApi;

  public RoomPresenter(String roomId,
                       UserRepository userRepository,
                       MessageInteractor messageInteractor,
                       RoomRepository roomRepository,
                       MethodCallHelper methodCallHelper,
                       ConnectivityManagerApi connectivityManagerApi) {
    this.roomId = roomId;
    this.userRepository = userRepository;
    this.messageInteractor = messageInteractor;
    this.roomRepository = roomRepository;
    this.methodCallHelper = methodCallHelper;
    this.connectivityManagerApi = connectivityManagerApi;
  }

  @Override
  public void bindView(@NonNull RoomContract.View view) {
    super.bindView(view);

    getRoomInfo();
    getRoomHistoryStateInfo();
    getMessages();
  }

  @Override
  public void loadMessages() {
    final Subscription subscription = getSingleRoom()
        .flatMap(messageInteractor::loadMessages)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            connectivityManagerApi.keepAliveServer();
          }
        });

    addSubscription(subscription);
  }

  @Override
  public void loadMoreMessages() {

    final Subscription subscription = getSingleRoom()
        .flatMap(messageInteractor::loadMoreMessages)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            connectivityManagerApi.keepAliveServer();
          }
        });

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
    final Subscription subscription = getRoomUserPair()
        .flatMap(pair -> messageInteractor.send(pair.first, pair.second, messageText))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            view.onMessageSendSuccessfully();
          }
        });

    addSubscription(subscription);
  }

  @Override
  public void resendMessage(Message message) {
    final Subscription subscription = messageInteractor.resend(message)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    addSubscription(subscription);
  }

  @Override
  public void deleteMessage(Message message) {
    final Subscription subscription = messageInteractor.delete(message)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    addSubscription(subscription);
  }

  @Override
  public void onUnreadCount() {
    final Subscription subscription = getRoomUserPair()
        .flatMap(roomUserPair -> messageInteractor
            .unreadCountFor(roomUserPair.first, roomUserPair.second))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            count -> view.showUnreadCount(count)
        );

    addSubscription(subscription);
  }

  @Override
  public void onMarkAsRead() {
    final Subscription subscription = roomRepository.getById(roomId)
        .first()
        .filter(room -> room != null && room.isAlert())
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> methodCallHelper.readMessages(room.getRoomId())
                .continueWith(new LogcatIfError())
        );

    addSubscription(subscription);
  }

  private void getRoomInfo() {
    final Subscription subscription = roomRepository.getById(roomId)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> view.render(room)
        );

    addSubscription(subscription);
  }

  private void getRoomHistoryStateInfo() {
    final Subscription subscription = roomRepository.getHistoryStateByRoomId(roomId)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            roomHistoryState -> {
              int syncState = roomHistoryState.getSyncState();
              view.updateHistoryState(
                  !roomHistoryState.isComplete(),
                  syncState == SyncState.SYNCED || syncState == SyncState.FAILED
              );
            }
        );

    addSubscription(subscription);
  }

  private void getMessages() {
    final Subscription subscription = roomRepository.getById(roomId)
        .first()
        .flatMap(messageInteractor::getAllFrom)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(messages -> view.showMessages(messages));

    addSubscription(subscription);
  }

  private Single<Pair<Room, User>> getRoomUserPair() {
    return Single.zip(
        getSingleRoom(),
        userRepository.getCurrent()
            .filter(user -> user != null)
            .first()
            .toSingle(),
        Pair::new
    );
  }

  private Single<Room> getSingleRoom() {
    return roomRepository.getById(roomId)
        .first()
        .toSingle();
  }
}
