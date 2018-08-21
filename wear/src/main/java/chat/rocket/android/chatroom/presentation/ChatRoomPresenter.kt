package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.models.MessageMapperUtils
import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.markAsRead
import chat.rocket.core.internal.rest.messages
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject


class ChatRoomPresenter @Inject constructor(
    private val view: ChatRoomView,
    private val navigator: ChatRoomNavigator,
    private val mapper: MessageMapperUtils,
    private val serverInteractor: GetCurrentServerInteractor,
    private val factory: RocketChatClientFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val client = factory.create(currentServer)

    fun loadAndShowMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        launch {
            view.showLoading()
            try {
                val messages =
                    retryIO("messages chatRoom: $chatRoomId, type: $chatRoomType, offset: $offset") {
                        client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 7).result
                    }
                view.showMessages(mapper.map(messages = messages))
                markRoomAsRead(chatRoomId)
            } catch (ex: Exception) {
                Timber.e(ex)
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    private fun markRoomAsRead(roomId: String) {
        launch {
            try {
                retryIO(description = "markAsRead($roomId)") { client.markAsRead(roomId) }
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }
}