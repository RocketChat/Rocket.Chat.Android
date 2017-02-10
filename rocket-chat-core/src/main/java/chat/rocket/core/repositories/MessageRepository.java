package chat.rocket.core.repositories;

import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import rx.Observable;
import rx.Single;

public interface MessageRepository {

  Single<Message> getById(String messageId);

  Single<Boolean> save(Message message);

  Single<Boolean> resend(Message message);

  Single<Boolean> delete(Message message);

  Observable<Message> getAllFrom(Room room);

  Single<Integer> unreadCountFrom(Room room);
}
