package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.behaviour.LoadingView
import chat.rocket.android.core.behaviour.MessagesView
import chat.rocket.core.model.ChatRoom

interface ChatRoomsView : LoadingView, MessagesView {
    fun updateChatRooms(chatRooms: List<ChatRoom>)
}