package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatRoomsView : LoadingView, MessageView {
    fun showLoadingRoom(name: CharSequence)

    fun hideLoadingRoom()
}