package chat.rocket.android.chatroom.reply.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
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
                client.sendMessage(id, chatRoomId, text)
                view.messageSentSuccessfully()
            } catch (ex: Exception) {
                //update this when this is updated in the mobile app
                // TODO - remove the generic message when we implement :userId:/message subscription
                if (ex is IllegalStateException) {
                    Timber.d(ex, "Probably a read-only problem...")
                    view.showGenericErrorMessage()
                } else {
                    // some other error, just rethrow it...
                    throw ex
                }
                view.hideLoading()
            }
        }
    }
}