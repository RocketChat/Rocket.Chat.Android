package chat.rocket.android.helper

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.useRealName
import chat.rocket.common.model.SimpleUser
import chat.rocket.common.model.User
import javax.inject.Inject

class UserHelper @Inject constructor(
    private val localRepository: LocalRepository,
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val settingsRepository: SettingsRepository
) {

    /**
     * Return current logged [User].
     */
    fun user(): User? = getCurrentServerInteractor.get()?.let { localRepository.getCurrentUser(it) }

    /**
     * Return the username for the current logged [User].
     */
    fun username(): String? = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY, null)

    /**
     * Return the display name for the given [user].
     * If setting 'Use_Real_Name' is true then the real name will be given, otherwise the username
     * without the '@' is yielded.
     */
    fun displayName(user: SimpleUser) = getCurrentServerInteractor.get()?.let {
        if (settingsRepository.get(it).useRealName()) {
            user.name
        } else {
            user.username
        }
    }.orEmpty()

    /**
     * Whether current [User] is admin on the current server.
     */
    fun isAdmin(): Boolean = user()?.roles?.find { it.equals("admin", true) } != null
}
