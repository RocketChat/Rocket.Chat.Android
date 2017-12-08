package chat.rocket.android.app.chatroom

import chat.rocket.android.app.User
import org.threeten.bp.LocalTime

data class Message(val user: User, val content: String, val time: LocalTime)