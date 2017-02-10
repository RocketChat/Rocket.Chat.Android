package chat.rocket.core.repositories;

import java.util.List;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import rx.Observable;
import rx.Single;

public interface MessageRepository {

  Single<Message> getById(String messageId);

  Single<Boolean> save(Message message);

  Single<Boolean> resend(Message message);

  Single<Boolean> delete(Message message);

  Observable<List<Message>> getAllFrom(Room room);

  Single<Integer> unreadCountFor(Room room, User user);
}
