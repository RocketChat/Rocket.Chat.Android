package chat.rocket.android.app.chatlist

import org.threeten.bp.LocalDateTime

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
data class Chat(val userAvatarUri: String,
                val name: String,
                val type: String,
                val userStatus: String?,
                val lastMessage: String,
                val lastMessageDateTime: LocalDateTime,
                val totalUnreadMessages: Int)