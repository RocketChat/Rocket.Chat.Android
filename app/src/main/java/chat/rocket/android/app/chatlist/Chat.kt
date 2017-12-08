package chat.rocket.android.app.chatlist

import org.threeten.bp.LocalDateTime

data class Chat(val userAvatarUri: String,
data class Chat(val user: User,
                val name: String,
                val type: String,
                val userStatus: String?,
                val lastMessage: String,
                val lastMessageDateTime: LocalDateTime,
                val totalUnreadMessages: Int)