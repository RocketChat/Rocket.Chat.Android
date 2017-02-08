package chat.rocket.android.repositories;

import chat.rocket.android.model.core.User;
import rx.Observable;

public interface UserRepository {

  Observable<User> getCurrentUser();
}
