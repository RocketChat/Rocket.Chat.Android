package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.chatRooms
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

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
                val chatRooms = retryIO("ChatRooms") { client.chatRooms(timestamp, filterCustom) }
                view.updateChatRooms(chatRooms.update)
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
}
