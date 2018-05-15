package chat.rocket.android.helper

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.useRealName
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Message
import javax.inject.Inject

class MessageHelper @Inject constructor(
    getSettingsInteractor: GetSettingsInteractor,
    serverInteractor: GetCurrentServerInteractor
) {
    private val currentServer = serverInteractor.get()!!
    private val settings: PublicSettings = getSettingsInteractor.get(currentServer)

    fun createPermalink(message: Message, chatRoom: ChatRoom): String {
        val type = when (chatRoom.type) {
            is RoomType.PrivateGroup -> "group"
            is RoomType.Channel -> "channel"
            is RoomType.DirectMessage -> "direct"
            is RoomType.Livechat -> "livechat"
            else -> "custom"
        }
        val name = if (settings.useRealName()) chatRoom.fullName ?: chatRoom.name else chatRoom.name
        return "[ ]($currentServer/$type/$name?msg=${message.id}) "
    }

    fun messageIdFromPermalink(permalink: String): String? {
        PERMALINK_REGEX.find(permalink.trim())?.let {
            if (it.groupValues.size == 4) {
                return it.groupValues[3]
            }
        }
        return null
    }

    fun roomTypeFromPermalink(permalink: String): String? {
        PERMALINK_REGEX.find(permalink.trim())?.let {
            if (it.groupValues.size == 4) {
                val type = it.groupValues[2]
                return when(type) {
                    "group" -> "p"
                    "channel" -> "c"
                    "direct" -> "d"
                    "livechat" -> "l"
                    else -> type
                }
            }
        }
        return null
    }

    companion object {
        val PERMALINK_REGEX = "(?:__|[*#])|\\[(.+?)\\]\\(.+?//.+?/(.+)/.+\\?.*=(.*)\\)".toRegex()
    }
}