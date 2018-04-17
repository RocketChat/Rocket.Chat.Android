package chat.rocket.android.server.domain

import chat.rocket.common.model.User
import javax.inject.Inject

class GetActiveUsersInteractor @Inject constructor(private val repository: ActiveUsersRepository) {

    fun getActiveUserById(url: String, id: String): User? {
        return repository.get(url).find { user -> user.id == id }
    }

    fun getActiveUserByUsername(url: String, username: String): User? {
        return repository.get(url).find { user -> user.username == username }
    }
}