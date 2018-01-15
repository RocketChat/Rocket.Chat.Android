package chat.rocket.android.chatroom.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.subscribeRoomMessages
import chat.rocket.core.internal.realtime.unsubscibre
import chat.rocket.core.internal.rest.messages
import chat.rocket.core.internal.rest.sendMessage
import chat.rocket.core.model.Message
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(private val view: ChatRoomView,
                                            private val strategy: CancelStrategy,
                                            private val serverInteractor: GetCurrentServerInteractor,
                                            factory: RocketChatClientFactory) {
    private val client = factory.create(serverInteractor.get()!!)
    private val roomMessages = ArrayList<Message>()
    private var subId: String? = null

    fun loadMessages(chatRoomId: String, chatRoomType: String, offset: Int = 0) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val messages = client.messages(chatRoomId, BaseRoom.RoomType.valueOf(chatRoomType), offset.toLong(), 30).result
                synchronized(roomMessages) {
                    roomMessages.addAll(messages)
                }
                view.showMessages(messages, serverInteractor.get()!!)
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
                synchronized(roomMessages) {
                    roomMessages.add(0, message)
                }
                view.showNewMessage(message)
            } catch (ex: Exception) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun subscribeMessages(roomId: String) {
        launchUI(strategy) {
            subId = client.subscribeRoomMessages(roomId) {
                Timber.d("subscribe messages for $roomId: $it")
            }
            listenMessages(roomId)
        }
    }

    fun unsubscribeMessages() {
        launch(CommonPool) {
            subId?.let { subscriptionId ->
                client.unsubscibre(subscriptionId)
            }
        }
    }

    private suspend fun listenMessages(roomId: String) {
        launch(CommonPool + strategy.jobs) {
            for (message in client.messagesChannel) {
                if (message.roomId != roomId) {
                    Timber.d("Ignoring message for room ${message.roomId}, expecting $roomId")
                }

                updateMessage(message)
            }
        }
    }

    private fun updateMessage(streamedMessage: Message) {
        launchUI(strategy) {
            synchronized(roomMessages) {
                val index = roomMessages.indexOfFirst { msg -> msg.id == streamedMessage.id }
                if (index != -1) {
                    Timber.d("Updatind message at $index")
                    roomMessages[index] = streamedMessage
                    view.dispatchUpdateMessage(index, streamedMessage)
                } else {
                    Timber.d("Adding new message")
                    roomMessages.add(0, streamedMessage)
                    view.showNewMessage(streamedMessage)
                }
            }
        }
    }
}