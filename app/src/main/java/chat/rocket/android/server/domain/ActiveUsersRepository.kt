package chat.rocket.android.server.domain

import chat.rocket.common.model.User

interface ActiveUsersRepository {

    fun save(url: String, activeUsers: List<User>)

    fun get(url: String): List<User>
}