package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val navigator: ChatRoomsNavigator,
                                             serverInteractor: GetCurrentServerInteractor,
                                             factory: RocketChatClientFactory) {
    private val client = factory.create(serverInteractor.get()!!)

    fun loadChatRooms() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val chatRooms = client.chatRooms().update
                val openChatRooms = getOpenChatRooms(chatRooms)
                val sortedOpenChatRooms = sortChatRooms(openChatRooms)
                view.showChatRooms(sortedOpenChatRooms.toMutableList())
            } catch (ex: Exception) {
                view.showMessage(ex.message!!)
            } finally {
                view.hideLoading()
            }
        }
    }

    fun loadChatRoom(chatRoom: ChatRoom) {
        navigator.toChatRoom(chatRoom.id, chatRoom.name, chatRoom.type.name, chatRoom.open)
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun sortChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending { chatRoom ->
            chatRoom.lastMessage?.timestamp
        }
    }
}