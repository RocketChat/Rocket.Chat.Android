package chat.rocket.android.server.domain

import chat.rocket.common.model.User
import javax.inject.Inject

class SaveActiveUsersInteractor @Inject constructor(private val repository: ActiveUsersRepository) {

    fun save(url: String, activeUsers: List<User>) = repository.save(url, activeUsers)
}