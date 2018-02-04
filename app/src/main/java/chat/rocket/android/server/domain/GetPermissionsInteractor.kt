package chat.rocket.android.server.domain

import javax.inject.Inject

class GetPermissionsInteractor @Inject constructor(private val settingsRepository: SettingsRepository,
                                                   private val currentServerRepository: CurrentServerRepository) {

    private fun publicSettings(): PublicSettings? = settingsRepository.get(currentServerRepository.get()!!)

    /**
     * Check whether user is allowed to delete a message.
     */
    fun isMessageDeletingAllowed() = publicSettings()?.deleteMessageAllowed() ?: false

    /**
     * Checks whether user is allowed to edit a message.
     */
    fun isMessageEditingAllowed() = publicSettings()?.deleteMessageAllowed() ?: false

    /**
     * Checks whether should show deleted message status.
     */
    fun showDeletedStatus() = publicSettings()?.showDeletedStatus() ?: false

    /**
     * Checks whether should show edited message status.
     */
    fun showEditedStatus() = publicSettings()?.showEditedStatus() ?: false
}