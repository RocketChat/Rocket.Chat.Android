package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SaveChatRoomsInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.async
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val serverInteractor: GetCurrentServerInteractor,
                                             private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                             private val saveChatRoomsInteractor: SaveChatRoomsInteractor,
                                             private val factory: RocketChatClientFactory) {
    lateinit var client: RocketChatClient

    fun chatRooms() {
        // TODO - check for current server
        val currentServer = serverInteractor.get()!!
        client = factory.create(currentServer)
        launchUI(strategy) {
            view.showLoading()
            val chatRooms = client.chatRooms().update
            val openChatRooms = getOpenChatRooms(chatRooms)
            val sortedOpenChatRooms = sortChatRooms(openChatRooms)
            saveChatRoomsInteractor.save(currentServer, sortedOpenChatRooms)
            view.showChatRooms(sortedOpenChatRooms.toMutableList())
            view.hideLoading()
        }
    }

    /**
     * Get a ChatRoom list from local repository. ChatRooms returned are filtered by name.
     */
    fun chatRoomsByName(name: String) {
        val currentServer = serverInteractor.get()!!
        launchUI(strategy) {
            val roomList = getChatRoomsInteractor.getByName(currentServer, name)
            view.showChatRooms(roomList.toMutableList())
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