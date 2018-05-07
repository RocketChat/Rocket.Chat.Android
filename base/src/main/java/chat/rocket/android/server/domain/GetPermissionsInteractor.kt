package chat.rocket.android.server.domain

import javax.inject.Inject

class GetPermissionsInteractor @Inject constructor(private val settingsRepository: SettingsRepository,
                                                   private val currentServerRepository: CurrentServerRepository) {

    private fun publicSettings(): PublicSettings? = settingsRepository.get(currentServerRepository.get()!!)

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
}