package chat.rocket.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;

import chat.rocket.core.models.User;

public interface UserRepository {

  Flowable<Optional<User>> getCurrent();
}
