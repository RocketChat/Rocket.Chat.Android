package chat.rocket.android.server.domain

import chat.rocket.android.helper.ChatRoomRoleHelper
import chat.rocket.android.helper.UserHelper
import javax.inject.Inject

// Creating rooms
private const val CREATE_PUBLIC_CHANNELS = "create-c"
private const val CREATE_DIRECT_MESSAGES = "create-d"
private const val CREATE_PRIVATE_CHANNELS = "create-p"

// Add/Remove user
const val REMOVE_USER = "remove-user"
const val ADD_USER_TO_JOINED_ROOM = "add-user-to-joined-room"
const val ADD_USER_TO_ANY_CHANNEL_ROOM = "add-user-to-any-c-room"
const val ADD_USER_TO_ANY_PRIVATE_ROOM = "add-user-to-any-p-room"

// Messages
private const val DELETE_MESSAGE = "delete-message"
private const val FORCE_DELETE_MESSAGE = "force-delete-message"
private const val EDIT_MESSAGE = "edit-message"
private const val PIN_MESSAGE = "pin-message"
private const val POST_READONLY = "post-readonly"

private const val VIEW_STATISTICS = "view-statistics"
private const val VIEW_ROOM_ADMINISTRATION = "view-room-administration"
private const val VIEW_USER_ADMINISTRATION = "view-user-administration"
private const val VIEW_PRIVILEGED_SETTING = "view-privileged-setting"

class PermissionsInteractor @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val permissionsRepository: PermissionsRepository,
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val userHelper: UserHelper,
    private val chatRoomRoleHelper: ChatRoomRoleHelper
) {
    private fun publicSettings(): PublicSettings? = settingsRepository.get(currentServerUrl()!!)

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

    fun isAdministrationEnabled(): Boolean {
        currentServerUrl()?.let { serverUrl ->
            val viewStatistics =
                permissionsRepository.get(serverUrl, VIEW_STATISTICS)
            val viewRoomAdministration =
                permissionsRepository.get(serverUrl, VIEW_ROOM_ADMINISTRATION)
            val viewUserAdministration =
                permissionsRepository.get(serverUrl, VIEW_USER_ADMINISTRATION)
            val viewPrivilegedSetting =
                permissionsRepository.get(serverUrl, VIEW_PRIVILEGED_SETTING)

            userHelper.user()?.roles?.let { userRolesList ->
                return viewStatistics?.roles?.any { userRolesList.contains(it) } == true ||
                        viewRoomAdministration?.roles?.any { userRolesList.contains(it) } == true ||
                        viewUserAdministration?.roles?.any { userRolesList.contains(it) } == true ||
                        viewPrivilegedSetting?.roles?.any { userRolesList.contains(it) } == true
            }
        }
        return false
    }

    suspend fun hasPermission(permissionType: String, chatRoomId: String): Boolean {
        val permissionRoles = getPermissionRoles(permissionType)
        val chatRoomRoles = chatRoomRoleHelper.getChatRoles(chatRoomId)

        val currentUserRoles: List<String>? =
            chatRoomRoles.firstOrNull { it.user.username == userHelper.username() }?.roles
        return if (currentUserRoles.isNullOrEmpty() || permissionRoles.isNullOrEmpty()) {
            false
        } else {
            currentUserRoles.intersect(permissionRoles).isNotEmpty()
        }
    }

    private fun getPermissionRoles(permissionType: String): List<String>? {
        val url = getCurrentServerInteractor.get()!!
        var permissionRoles: List<String> = emptyList()

        permissionsRepository.get(url, permissionType)?.let { permission ->
            permissionRoles = permission.roles
        }

        return permissionRoles
    }

    private fun currentServerUrl(): String? {
        return getCurrentServerInteractor.get()
    }
}