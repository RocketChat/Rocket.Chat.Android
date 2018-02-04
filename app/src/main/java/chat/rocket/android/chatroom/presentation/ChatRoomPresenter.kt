package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.viewmodel.MessageViewModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.State
import chat.rocket.core.internal.realtime.connect
import chat.rocket.core.internal.realtime.subscribeRoomMessages
import chat.rocket.core.internal.realtime.unsubscibre
import chat.rocket.core.internal.rest.deleteMessage
import chat.rocket.core.internal.rest.messages
import chat.rocket.core.internal.rest.sendMessage
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.cancel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(private val view: ChatRoomView,
                                            private val strategy: CancelStrategy,
                                            getSettingsInteractor: GetSettingsInteractor,
                                            private val serverInteractor: GetCurrentServerInteractor,
                                            private val permissionsInteractor: PermissionsInteractor,
                                            private val messagesRepository: MessagesRepository,
                                            factory: RocketChatClientFactory,
                                            private val mapper: MessageViewModelMapper) {
    private val client = factory.create(serverInteractor.get()!!)
    private var subId: String? = null
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)!!

    private val stateChannel = Channel<State>()

    fun loadMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val messages = client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 30).result
                messagesRepository.saveAll(messages)

                val messagesViewModels = mapper.mapToViewModelList(messages, settings)
                view.showMessages(messagesViewModels, serverInteractor.get()!!)

                // Subscribe after getting the first page of messages from REST
                if (offset == 0L) {
                    subscribeMessages(chatRoomId)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
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
            view.disableMessageInput()
            try {
                val message = client.sendMessage(chatRoomId, text)
                // ignore message for now, will receive it on the stream
                view.enableMessageInput(clear = true)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }

                view.enableMessageInput()
            }
        }
    }

    fun subscribeMessages(roomId: String) {
        client.addStateChannel(stateChannel)
        launch(CommonPool + strategy.jobs) {
            for (status in stateChannel) {
                Timber.d("Changing status to: $status")
                when (status) {
                    State.Authenticating -> Timber.d("Authenticating")
                    State.Connected -> {
                        Timber.d("Connected")
                        subId = client.subscribeRoomMessages(roomId) {
                            Timber.d("subscribe messages for $roomId: $it")
                        }
                    }
                }
            }
            Timber.d("Done on statusChannel")
        }

        when (client.state) {
            State.Connected -> {
                Timber.d("Already connected")
                subId = client.subscribeRoomMessages(roomId) {
                    Timber.d("subscribe messages for $roomId: $it")
                }
            }
            else -> client.connect()
        }

        launchUI(strategy) {
            listenMessages(roomId)
        }

        // TODO - when we have a proper service, we won't need to take care of connection, just
        // subscribe and listen...
        /*launchUI(strategy) {
            subId = client.subscribeRoomMessages(roomId) {
                Timber.d("subscribe messages for $roomId: $it")
            }
            listenMessages(roomId)
        }*/
    }

    fun unsubscribeMessages() {
        launch(CommonPool) {
            client.removeStateChannel(stateChannel)
            subId?.let { subscriptionId ->
                client.unsubscibre(subscriptionId)
            }
        }
    }

    /**
     * Delete the message with the given id.
     *
     * @param roomId The room id of the message to be deleted.
     * @param id The id of the message to be deleted.
     */
    fun deleteMessage(roomId: String, id: String) {
        launchUI(strategy) {
            if (!permissionsInteractor.isMessageDeletingAllowed()) {
                coroutineContext.cancel()
                return@launchUI
            }
            //TODO: Default delete message always to true. Until we have the permissions system
            //implemented, a user will only be able to delete his own messages.
            try {
                //TODO: Should honor permission 'Message_ShowDeletedStatus'
                client.deleteMessage(roomId, id, true)
                // if Message_ShowDeletedStatus == true an update to that message will be dispatched.
                // Otherwise we signalize that we just want the message removed.
                if (!permissionsInteractor.showDeletedStatus()) {
                    view.dispatchDeleteMessage(id)
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
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
            val viewModelStreamedMessage = mapper.mapToViewModel(streamedMessage, settings)
            val roomMessages = messagesRepository.getByRoomId(streamedMessage.roomId)
            val index = roomMessages.indexOfFirst { msg -> msg.id == streamedMessage.id }
            if (index > -1) {
                Timber.d("Updating message at $index")
                messagesRepository.save(streamedMessage)
                view.dispatchUpdateMessage(index, viewModelStreamedMessage)
            } else {
                Timber.d("Adding new message")
                messagesRepository.save(streamedMessage)
                view.showNewMessage(viewModelStreamedMessage)
            }
        }
    }
}