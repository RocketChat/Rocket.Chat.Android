package chat.rocket.android.server.domain

import chat.rocket.common.model.Email
import chat.rocket.common.model.User
import chat.rocket.common.model.UserStatus

interface UsersRepository {
    /**
     * Get all users. Use carefully!
     *
     * @return All users or an empty list.
     */
    fun getAll(): List<User>

    fun get(query: Query.() -> Unit): List<User>
    /**
     * Save a single user object.
     *
     * @param user The user object to save.
     */
    fun save(user: User)
    /**
     * Save a list of users.
     *
     * @param users The list of users to save.
     */
    fun saveAll(userList: List<User>)
    /**
     * Removes all users.
     */
    fun clear()

    data class Query(
            var id: String? = null,
            var name: String? = null,
            var username: String? = null,
            var emails: List<Email>? = null,
            var utfOffset: Float? = null,
            var status: UserStatus? = null,
            var limit: Long = 0L
    )
}