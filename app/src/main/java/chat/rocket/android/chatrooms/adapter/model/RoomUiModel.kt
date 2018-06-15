package chat.rocket.android.chatrooms.adapter.model

import chat.rocket.common.model.RoomType
import chat.rocket.common.model.UserStatus

data class RoomUiModel(
    val id: String,
    val type: RoomType,
    val name: CharSequence,
    val avatar: String,
    val date: CharSequence?,
    val unread: String?,
    val alert: Boolean,
    val lastMessage: CharSequence?,
    val status: UserStatus?
)