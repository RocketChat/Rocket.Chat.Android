package chat.rocket.android.server.domain

import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.core.model.Permission
import javax.inject.Inject

// Creating rooms
const val CREATE_PUBLIC_CHANNELS = "create-c"
const val CREATE_DIRECT_MESSAGES = "create-d"
const val CREATE_PRIVATE_CHANNELS = "create-p"

// Messages
const val DELETE_MESSAGE = "delete-message"
const val FORCE_DELETE_MESSAGE = "force-delete-message"
const val EDIT_MESSAGE = "edit-message"
const val PIN_MESSAGE = "pin-message"
const val POST_READONLY = "post-readonly"

class PermissionsInteractor @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val permissionsRepository: PermissionsRepository,
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val userHelper: UserHelper
) {

    private fun publicSettings(): PublicSettings? = settingsRepository.get(currentServerUrl()!!)

    fun saveAll(permissions: List<Permission>) {
        val url = currentServerUrl()!!
        permissions.forEach { permissionsRepository.save(url, it) }
    }

    /**
     * Check whether the user is allowed to delete a message.
     */
    fun allowedMessageDeleting() = publicSettings()?.allowedMessageDeleting() ?: false

    /**
     * Checks whether the user is allowed to edit a message.
     */
    fun allowedMessageEditing() = publicSettings()?.allowedMessageEditing() ?: false

    /**
     * Checks whether the user is allowed to pin a message to a channel.
     */
    fun allowedMessagePinning() = publicSettings()?.allowedMessagePinning() ?: false

    /**
     * Checks whether the user is allowed to star a message.
     */
    fun allowedMessageStarring() = publicSettings()?.allowedMessageStarring() ?: false

    /**
     * Checks whether should show deleted message status.
     */
    fun showDeletedStatus() = publicSettings()?.showDeletedStatus() ?: false

    /**
     * Checks whether should show edited message status.
     */
    fun showEditedStatus() = publicSettings()?.showEditedStatus() ?: false

    fun canPostToReadOnlyChannels(): Boolean {
        val url = getCurrentServerInteractor.get()!!
        val currentUserRoles = userHelper.user()?.roles
        return permissionsRepository.get(url, POST_READONLY)?.let { permission ->
            currentUserRoles?.isNotEmpty() == true && permission.roles.any {
                currentUserRoles.contains(it)
            }
        } == true || userHelper.isAdmin()
    }

    private fun currentServerUrl(): String? {
        return getCurrentServerInteractor.get()
    }
}