package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.ActiveUsersRepository
import chat.rocket.common.model.User

class MemoryActiveUsersRepository : ActiveUsersRepository {

    val cache = HashMap<String, List<User>>()

    override fun save(url: String, activeUsers: List<User>) {
        cache[url] = activeUsers
    }

    override fun get(url: String): List<User> = cache[url] ?: emptyList()
}