package chat.rocket.android.app.chatlist

data class Chat (val userAvatarUri: String, val roomName : String, val lastMessage : String, val lastMessageTimestamp: String, val unreadMessage: Int)