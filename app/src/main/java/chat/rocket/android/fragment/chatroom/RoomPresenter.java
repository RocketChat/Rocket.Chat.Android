package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;

import java.util.UUID;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.core.Message;
import chat.rocket.android.model.core.RoomHistoryState;
import chat.rocket.android.repositories.core.MessageRepository;
import chat.rocket.android.repositories.core.RoomRepository;
import chat.rocket.android.repositories.core.UserRepository;
import chat.rocket.android.service.ConnectivityManagerApi;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class RoomPresenter implements RoomContract.Presenter {

  private final String roomId;
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final MessageRepository messageRepository;
  private final ConnectivityManagerApi connectivityManagerApi;

  private CompositeSubscription compositeSubscription = new CompositeSubscription();
  private RoomContract.View view;

  public RoomPresenter(String roomId, UserRepository userRepository,
                       RoomRepository roomRepository,
                       MessageRepository messageRepository,
                       ConnectivityManagerApi connectivityManagerApi) {
    this.roomId = roomId;
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.messageRepository = messageRepository;
    this.connectivityManagerApi = connectivityManagerApi;
  }

  @Override
  public void bindView(@NonNull RoomContract.View view) {
    this.view = view;

    getRoomInfo();
    getRoomHistoryStateInfo();
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
}
