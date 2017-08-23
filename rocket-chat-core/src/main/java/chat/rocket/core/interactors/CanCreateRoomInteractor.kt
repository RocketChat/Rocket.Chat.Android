package chat.rocket.core.interactors

import chat.rocket.core.models.Session
import chat.rocket.core.models.User
import io.reactivex.Flowable
import io.reactivex.Single

import chat.rocket.core.repositories.UserRepository
import com.hadisatrio.optional.Optional
import io.reactivex.functions.Function3

class CanCreateRoomInteractor(private val userRepository: UserRepository,
                              private val sessionInteractor: SessionInteractor) {

    fun canCreate(roomId: String): Single<Boolean> {
        return Flowable.zip<Optional<User>, Optional<Session>, String, Boolean>(
                userRepository.getCurrent(),
                sessionInteractor.getDefault(),
                Flowable.just(roomId),
                Function3 { user, session, room -> user.isPresent && session.isPresent && room != null }
        )
                .first(false)
    }
}
