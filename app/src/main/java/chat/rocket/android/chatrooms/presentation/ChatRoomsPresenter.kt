package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView, private val strategy: CancelStrategy) {
    @Inject lateinit var client: RocketChatClient

    fun chatRooms() {
        launchUI(strategy) {
            view.showLoading()
            val chatRooms = client.chatRooms().update
            val openChatRooms = getOpenChatRooms(chatRooms)
            val sortedOpenChatRooms = sortChatRooms(openChatRooms)
            view.showChatRooms(sortedOpenChatRooms.toMutableList())
            view.hideLoading()
        }
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun sortChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending {
            chatRoom -> chatRoom.lastMessage?.timestamp
        }
    }
}