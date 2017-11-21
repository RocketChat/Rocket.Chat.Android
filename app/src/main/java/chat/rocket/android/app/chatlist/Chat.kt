package chat.rocket.android.app.chatlist

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
data class Chat(val userAvatarUri: String,
                val name: String,
                val type: String,
                val userStatus: String?,
                val lastMessage: String,
                val lastMessageTimestamp: String,
                val totalUnreadMessages: Int)