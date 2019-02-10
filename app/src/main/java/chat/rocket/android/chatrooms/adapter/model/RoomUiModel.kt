package chat.rocket.android.chatrooms.adapter.model

import chat.rocket.common.model.RoomType
import chat.rocket.common.model.UserStatus

data class RoomUiModel(
    val id: String,
    val type: RoomType,
    val name: CharSequence,
    val avatar: String,
    val open: Boolean = false,
    val date: CharSequence? = null,
    val unread: String? = null,
    val alert: Boolean = false,
    val favorite: Boolean? = false,
    val mentions: Boolean = false,
    val lastMessage: CharSequence? = null,
    val status: UserStatus? = null,
    val username: String? = null,
    val broadcast: Boolean = false,
    val canModerate: Boolean = false,
    val writable: Boolean = true,
    val muted: List<String> = emptyList()
)
