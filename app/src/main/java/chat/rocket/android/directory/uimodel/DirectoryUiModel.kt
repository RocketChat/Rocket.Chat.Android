package chat.rocket.android.directory.uimodel

import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.common.model.Token
import chat.rocket.core.model.DirectoryResult

class DirectoryUiModel(
    private val directoryResult: DirectoryResult,
    private val baseUrl: String?,
    private val token: Token?
) {
    val id: String = directoryResult.id
    val channelAvatarUri: String?
    val userAvatarUri: String?
    val name: String = directoryResult.name
    val username: String = "@${directoryResult.username}"
    val serverUrl: String = "" // TODO
    val description: String = "" // TODO
    val totalMembers: String = "" // TODO

    init {
        channelAvatarUri = getChannelAvatar()
        userAvatarUri = getUserAvatar()
    }

    private fun getChannelAvatar(): String? {
        return baseUrl?.avatarUrl(name, token?.userId, token?.authToken, isGroupOrChannel = true)
    }

    private fun getUserAvatar(): String? {
        return directoryResult.username?.let {
            baseUrl?.avatarUrl(it, token?.userId, token?.authToken)
        }
    }
}
