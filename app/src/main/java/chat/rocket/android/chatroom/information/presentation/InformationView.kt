package chat.rocket.android.chatroom.information.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.core.model.ChatRoom

interface InformationView: LoadingView, MessageView {
    fun showRoomInfo(room: ChatRoom)
    fun onLeave()
    fun onHide()
    fun allowRoomEditing()
    fun allowHideAndLeave(isOpen: Boolean)
}
