package chat.rocket.android.chatroom.edit.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.widget.roomupdate.RoomUpdateListener
import chat.rocket.core.model.ChatRoom

interface EditInfoView: MessageView, LoadingView {
    fun showRoomInfo(room: ChatRoom)
}