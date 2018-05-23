package chat.rocket.android.chatroom.viewmodel

import chat.rocket.core.model.ChatRoomRole

data class RoomViewModel(
    val roles: List<ChatRoomRole>,
    val isBroadcast: Boolean = false,
    val isRoom: Boolean = false
)