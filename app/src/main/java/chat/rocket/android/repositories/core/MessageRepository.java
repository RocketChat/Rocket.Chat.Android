package chat.rocket.android.repositories.core;

import chat.rocket.android.model.core.Message;
import chat.rocket.android.model.core.Room;
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
