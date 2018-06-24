package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.Constants
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KProperty1

class ChatRoomsPresenter @Inject constructor(
    private val view: ChatRoomsView,
    private val serverInteractor: GetCurrentServerInteractor,
    private val factory: RocketChatClientFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val client = factory.create(currentServer)
    fun loadChatRooms(timestamp: Long = 0, filterCustom: Boolean = true) {
        launch {
            view.showLoading()
            try {
                view.updateChatRooms(getUserChatRooms(timestamp, filterCustom))
            } catch (ex: RocketChatException) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
                Timber.e(ex)
            } finally {
                view.hideLoading()
            }
        }
    }

    private suspend fun getUserChatRooms(timestamp: Long, filterCustom: Boolean): List<ChatRoom> {
        val chatRooms = retryIO("ChatRooms") { client.chatRooms(timestamp, filterCustom).update }
        return sortRooms(chatRooms)
    }

    private fun sortRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val openChatRooms = getOpenChatRooms(chatRooms)
        return openChatRooms.sortedWith(compareBy(ChatRoom::type).thenByDescending { chatroom ->
            chatroom.updatedAt
        })
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun compareBy(selector: KProperty1<ChatRoom, RoomType>): Comparator<ChatRoom> {
        return Comparator { a, b -> getTypeConstant(a.type) - getTypeConstant(b.type) }
    }

    private fun getTypeConstant(roomType: RoomType): Int {
        return when (roomType) {
            is RoomType.Channel -> Constants.CHATROOM_CHANNEL
            is RoomType.PrivateGroup -> Constants.CHATROOM_PRIVATE_GROUP
            is RoomType.DirectMessage -> Constants.CHATROOM_DM
        //is RoomType.Livechat -> Constants.CHATROOM_LIVE_CHAT
            else -> 0
        }
    }
}
