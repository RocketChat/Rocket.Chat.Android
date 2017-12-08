package chat.rocket.android.app.chatlist

import chat.rocket.android.app.User
import org.threeten.bp.LocalDateTime

data class Chat(val user: User,
                val name: String,
                val type: String,
                // Todo replace to Message type instead of String
                val lastMessage: String,
                val lastMessageDateTime: LocalDateTime,
                val totalUnreadMessages: Int)