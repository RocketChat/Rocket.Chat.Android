package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.models.MessageMapperUtils
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.messages
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject


class ChatRoomPresenter @Inject constructor(
    private val view: ChatRoomView,
    private val navigator: ChatRoomNavigator,
    private val strategy: CancelStrategy,
    private val mapper: MessageMapperUtils,
    private val serverInteractor: GetCurrentServerInteractor,
    private val factory: RocketChatClientFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val client = factory.create(currentServer)

    fun loadAndShowMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val messages =
                    retryIO("messages chatRoom: $chatRoomId, type: $chatRoomType, offset: $offset") {
                        client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 7).result
                    }
                view.showMessages(mapper.map(messages = messages))
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
}