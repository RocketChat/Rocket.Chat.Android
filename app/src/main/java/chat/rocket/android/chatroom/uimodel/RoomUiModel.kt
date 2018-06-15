package chat.rocket.android.chatroom.uimodel

import chat.rocket.core.model.ChatRoomRole

data class RoomUiModel(
    val roles: List<ChatRoomRole>,
    val isBroadcast: Boolean = false,
    val isRoom: Boolean = false
)