package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.UUID;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.core.SyncState;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.RoomHistoryState;
import chat.rocket.core.repositories.MessageRepository;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.android.service.ConnectivityManagerApi;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class RoomPresenter implements RoomContract.Presenter {

  private final String roomId;
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final MessageRepository messageRepository;
  private final MethodCallHelper methodCallHelper;
  private final ConnectivityManagerApi connectivityManagerApi;

  private CompositeSubscription compositeSubscription = new CompositeSubscription();
  private RoomContract.View view;

  public RoomPresenter(String roomId, UserRepository userRepository,
                       RoomRepository roomRepository,
                       MessageRepository messageRepository,
                       MethodCallHelper methodCallHelper,
                       ConnectivityManagerApi connectivityManagerApi) {
    this.roomId = roomId;
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.messageRepository = messageRepository;
    this.methodCallHelper = methodCallHelper;
    this.connectivityManagerApi = connectivityManagerApi;
  }

  @Override
  public void bindView(@NonNull RoomContract.View view) {
    this.view = view;

    getRoomInfo();
    getRoomHistoryStateInfo();
    getMessages();
  }

  @Override
  public void release() {
    compositeSubscription.clear();
    this.view = null;
  }

  @Override
  public void loadMessages() {
    RoomHistoryState roomHistoryState = RoomHistoryState.builder()
        .setRoomId(roomId)
        .setSyncState(SyncState.NOT_SYNCED)
        .setCount(100)
        .setReset(true)
        .setComplete(false)
        .setTimestamp(0)
        .build();

    final Subscription subscription = roomRepository.setHistoryState(roomHistoryState)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            connectivityManagerApi.keepAliveServer();
          }
        });

    compositeSubscription.add(subscription);
  }

  @Override
  public void loadMoreMessages() {

    final Subscription subscription = roomRepository.getHistoryStateByRoomId(roomId)
        .filter(roomHistoryState -> {
          int syncState = roomHistoryState.getSyncState();
          return !roomHistoryState.isComplete()
              && (syncState == SyncState.SYNCED || syncState == SyncState.FAILED);
        })
        .first()
        .toSingle()
        .flatMap(roomHistoryState -> roomRepository
            .setHistoryState(roomHistoryState.withSyncState(SyncState.NOT_SYNCED)))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            connectivityManagerApi.keepAliveServer();
          }
        });

    compositeSubscription.add(subscription);
  }

  @Override
  public void sendMessage(String messageText) {
    final Subscription subscription = userRepository.getCurrentUser()
        .filter(user -> user != null)
        .first()
        .toSingle()
        .flatMap(user -> {
          Message message = Message.builder()
              .setId(UUID.randomUUID().toString())
              .setSyncState(SyncState.NOT_SYNCED)
              .setTimestamp(System.currentTimeMillis())
              .setRoomId(roomId)
              .setMessage(messageText)
              .setGroupable(false)
              .setUser(user)
              .build();

          return messageRepository.save(message);
        })
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
          if (success) {
            view.onMessageSendSuccessfully();
          }
        });

    compositeSubscription.add(subscription);
  }

  @Override
  public void resendMessage(String messageId) {
    final Subscription subscription = messageRepository.getById(messageId)
        .map(message -> message.withSyncState(SyncState.NOT_SYNCED))
        .flatMap(message -> messageRepository.resend(message))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    compositeSubscription.add(subscription);
  }

  @Override
  public void deleteMessage(String messageId) {
    final Subscription subscription = messageRepository.getById(messageId)
        .flatMap(message -> messageRepository.delete(message))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    compositeSubscription.add(subscription);
  }

  @Override
  public void onUnreadCount() {
    final Subscription subscription = Single.zip(
        userRepository.getCurrentUser()
            .filter(user -> user != null)
            .first()
            .toSingle(),
        roomRepository.getById(roomId)
            .first()
            .toSingle(),
        (user, room) -> new Pair<>(room, user)
    )
        .flatMap(roomUserPair -> messageRepository
            .unreadCountFor(roomUserPair.first, roomUserPair.second))
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            count -> view.showUnreadCount(count)
        );

    compositeSubscription.add(subscription);
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

    compositeSubscription.add(subscription);
  }

  private void getRoomInfo() {
    final Subscription subscription = roomRepository.getById(roomId)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> view.render(room)
        );

    compositeSubscription.add(subscription);
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

    compositeSubscription.add(subscription);
  }

  private void getMessages() {
    final Subscription subscription = roomRepository.getById(roomId)
        .first()
        .flatMap(messageRepository::getAllFrom)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(messages -> view.showMessages(messages));

    compositeSubscription.add(subscription);
  }
}
