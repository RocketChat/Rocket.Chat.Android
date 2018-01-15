package chat.rocket.android.chatroom.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.messages
import chat.rocket.core.internal.rest.sendMessage
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(private val view: ChatRoomView,
                                            private val strategy: CancelStrategy,
                                            private val serverInteractor: GetCurrentServerInteractor,
                                            factory: RocketChatClientFactory) {
    private val client = factory.create(serverInteractor.get()!!)

    fun loadMessages(chatRoomId: String, chatRoomType: String, offset: Int = 0) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val messages = client.messages(chatRoomId, BaseRoom.RoomType.valueOf(chatRoomType), offset.toLong(), 30).result
                view.showMessages(messages.toMutableList(), serverInteractor.get()!!)
            } catch (ex: Exception) {
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

    fun sendMessage(chatRoomId: String, text: String) {
        launchUI(strategy) {
            try {
                val message = client.sendMessage(chatRoomId, text)
                view.showSentMessage(message)
            } catch (ex: Exception) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }
}