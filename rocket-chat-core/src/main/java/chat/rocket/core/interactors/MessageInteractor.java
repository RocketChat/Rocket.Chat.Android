package chat.rocket.core.interactors;

import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import java.util.UUID;
import chat.rocket.core.SyncState;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.MessageRepository;
import chat.rocket.core.repositories.RoomRepository;

public class MessageInteractor {

  private final MessageRepository messageRepository;
  private final RoomRepository roomRepository;

  public MessageInteractor(MessageRepository messageRepository, RoomRepository roomRepository) {
    this.messageRepository = messageRepository;
    this.roomRepository = roomRepository;
  }

  public Single<Boolean> loadMessages(Room room) {
    final RoomHistoryState roomHistoryState = RoomHistoryState.builder()
        .setRoomId(room.getRoomId())
        .setSyncState(SyncState.NOT_SYNCED)
        .setCount(100)
        .setReset(true)
        .setComplete(false)
        .setTimestamp(0)
        .build();

    return roomRepository.setHistoryState(roomHistoryState);
  }

  public Single<Boolean> loadMoreMessages(Room room) {
    return roomRepository.getHistoryStateByRoomId(room.getRoomId())
        .filter(roomHistoryState -> {
          int syncState = roomHistoryState.getSyncState();
          return !roomHistoryState.isComplete()
              && (syncState == SyncState.SYNCED || syncState == SyncState.FAILED);
        })
        .firstElement()
        .toSingle()
        .flatMap(roomHistoryState -> roomRepository
            .setHistoryState(roomHistoryState.withSyncState(SyncState.NOT_SYNCED)));
  }

  public Single<Boolean> send(Room destination, User sender, String messageText) {
    final Message message = Message.builder()
        .setId(UUID.randomUUID().toString())
        .setSyncState(SyncState.NOT_SYNCED)
        .setTimestamp(System.currentTimeMillis())
        .setRoomId(destination.getRoomId())
        .setMessage(messageText)
        .setGroupable(false)
        .setUser(sender)
        .build();

    return messageRepository.save(message);
  }

  public Single<Boolean> resend(Message message) {
    return messageRepository.save(
        message.withSyncState(SyncState.NOT_SYNCED));
  }

  public Single<Boolean> delete(Message message) {
    return messageRepository.delete(message);
  }

  public Single<Integer> unreadCountFor(Room room, User user) {
    return messageRepository.unreadCountFor(room, user);
  }

  public Flowable<List<Message>> getAllFrom(Room room) {
    return messageRepository.getAllFrom(room);
  }
}
