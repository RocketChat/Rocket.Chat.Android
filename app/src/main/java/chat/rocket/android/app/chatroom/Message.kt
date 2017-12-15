package chat.rocket.android.app.chatroom

import chat.rocket.android.app.User
import org.threeten.bp.LocalDateTime

data class Message(val user: User,
                   val textContent: String?,
                   val imageAttachmentUri: String?,
                   val localDatetime: LocalDateTime)