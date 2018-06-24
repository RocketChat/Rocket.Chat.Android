package chat.rocket.android.util

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.LocalRepository
import chat.rocket.common.model.User
import javax.inject.Inject

class UserHelper @Inject constructor(
    private val localRepository: LocalRepository,
    private val getCurrentServerInteractor: GetCurrentServerInteractor
) {

    /**
     * Return the display name for the given [user].
     */
    fun displayName(user: User): String? {
        return user.username
    }

    /**
     * Return current logged user's display name.
     *
     * @see displayName
     */
    fun displayName(): String? {
        user()?.let {
            return displayName(it)
        }
        return null
    }

    /**
     * Return current logged [User].
     */
    fun user(): User? {
        return localRepository.getCurrentUser(serverUrl())
    }

    /**
     * Return the username for the current logged [User].
     */
    fun username(): String? = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY, null)

    private fun serverUrl(): String {
        return getCurrentServerInteractor.get()!!
    }
}