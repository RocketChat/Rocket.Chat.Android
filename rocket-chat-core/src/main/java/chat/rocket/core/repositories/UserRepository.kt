package chat.rocket.core.repositories

import com.hadisatrio.optional.Optional
import io.reactivex.Flowable
import chat.rocket.core.models.User

interface UserRepository {

    fun getAll(): Flowable<List<User>>

    fun getCurrent(): Flowable<Optional<User>>

    fun getByUsername(username: String): Flowable<Optional<User>>

    fun getSortedLikeName(name: String, limit: Int): Flowable<List<User>>
}