package chat.rocket.android.server.domain

import chat.rocket.common.model.User
import javax.inject.Inject

class SaveActiveUsersInteractor @Inject constructor(
    private val repository: ActiveUsersRepository,
    private val getActiveUsersInteractor: GetActiveUsersInteractor
) {

    fun save(url: String, activeUsers: List<User>) {
        repository.save(url, activeUsers)
    }

    fun addActiveUser(url: String, user: User) {
        val activeUserList: MutableList<User> =
            getActiveUsersInteractor.getAllActiveUsers(url).toMutableList()
        synchronized(this) {
            activeUserList.add(user)
        }
        save(url, activeUserList)
    }

    fun updateActiveUser(url: String, user: User) {
        getActiveUsersInteractor.getActiveUserById(url, user.id)?.let {
            val newUser = User(
                id = user.id,
                name = user.name ?: it.name,
                username = user.username ?: it.username,
                status = user.status ?: it.status,
                emails = user.emails ?: it.emails,
                utcOffset = user.utcOffset ?: it.utcOffset,
                roles = user.roles ?: it.roles
            )

            val activeUserList: MutableList<User> =
                getActiveUsersInteractor.getAllActiveUsers(url).toMutableList()
            synchronized(this) {
                activeUserList.removeAll { user_ -> user_.id == user.id }
            }
            activeUserList.add(newUser)
            save(url, activeUserList)
        }
    }
}