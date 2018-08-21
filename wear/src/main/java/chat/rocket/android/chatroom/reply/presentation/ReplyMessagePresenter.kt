package chat.rocket.android.chatroom.reply.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatNetworkErrorException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.sendMessage
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ReplyMessagePresenter @Inject constructor(
    private val view: ReplyMessageView,
    private val factory: RocketChatClientFactory,
    private val serverInteractor: GetCurrentServerInteractor
) {
    private lateinit var client: RocketChatClient
    private lateinit var currentServer: String

    fun sendMessage(chatRoomId: String, text: String) {
        currentServer = serverInteractor.get()!!
        client = factory.create(currentServer)
        launch {
            view.showLoading()
            try {
                val id = UUID.randomUUID().toString()
                retryIO("sending message") {
                    client.sendMessage(id, chatRoomId, text)
                }
                view.messageSentSuccessfully()
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
                view.hideLoading()

                if (ex is RocketChatNetworkErrorException)
                    view.showGenericErrorMessage()
            }
        }
    }
}