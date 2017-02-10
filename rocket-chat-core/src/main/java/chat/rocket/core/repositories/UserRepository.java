package chat.rocket.core.repositories;

import chat.rocket.core.models.User;
import rx.Observable;

public interface UserRepository {

  Observable<User> getCurrentUser();
}
