package chat.rocket.android.server.domain

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
    private val localRepository: LocalRepository,
    private val getCurrentServerInteractor: GetCurrentServerInteractor
) {

    private fun publicSettings(): PublicSettings? = settingsRepository.get(getCurrentServerUrl()!!)

    fun saveAll(permissions: List<Permission>) {
        val url = getCurrentServerUrl()!!
        permissions.forEach { permissionsRepository.save(url, it) }
    }

    /**
     * Check whether user is allowed to delete a message.
     */
    fun allowedMessageDeleting() = publicSettings()?.allowedMessageDeleting() ?: false

    /**
     * Checks whether user is allowed to edit a message.
     */
    fun allowedMessageEditing() = publicSettings()?.allowedMessageEditing() ?: false

    /**
     * Checks whether user is allowed to pin a message to a channel.
     */
    fun allowedMessagePinning() = publicSettings()?.allowedMessagePinning() ?: false

    /**
     * Checks whether should show deleted message status.
     */
    fun showDeletedStatus() = publicSettings()?.showDeletedStatus() ?: false

    /**
     * Checks whether should show edited message status.
     */
    fun showEditedStatus() = publicSettings()?.showEditedStatus() ?: false

    fun canPostToReadOnlyChannels(): Boolean {
        val url = getCurrentServerUrl()!!
        val currentUserRoles = localRepository.getCurrentUser(url)?.roles
        return permissionsRepository.get(url, POST_READONLY)?.let { permission ->
            currentUserRoles?.isNotEmpty() == true && permission.roles.any {
                currentUserRoles.contains(it)
            }
        } == true
    }

    private fun getCurrentServerUrl(): String? {
        return getCurrentServerInteractor.get()
    }
}