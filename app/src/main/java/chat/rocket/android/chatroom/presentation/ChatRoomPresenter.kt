package chat.rocket.android.chatroom.presentation

import android.net.Uri
import chat.rocket.android.R
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.chatroom.viewmodel.ViewModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.State
import chat.rocket.core.internal.realtime.connect
import chat.rocket.core.internal.realtime.subscribeRoomMessages
import chat.rocket.core.internal.realtime.unsubscribe
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(private val view: ChatRoomView,
                                            private val strategy: CancelStrategy,
                                            getSettingsInteractor: GetSettingsInteractor,
                                            private val serverInteractor: GetCurrentServerInteractor,
                                            private val permissions: GetPermissionsInteractor,
                                            private val uriInteractor: UriInteractor,
                                            private val messagesRepository: MessagesRepository,
                                            factory: RocketChatClientFactory,
                                            private val mapper: ViewModelMapper) {
    private val client = factory.create(serverInteractor.get()!!)
    private var subId: String? = null
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)!!
    private val stateChannel = Channel<State>()

    fun loadMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val messages =
                        client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 30).result
                messagesRepository.saveAll(messages)

                // TODO: For now we are marking the room as read if we can get the messages (I mean, no exception occurs)
                // but should mark only when the user see the first unread message.
                markRoomAsRead(chatRoomId)
              
                val messagesViewModels = mapper.map(messages)
                view.showMessages(messagesViewModels)

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

    fun sendMessage(chatRoomId: String, text: String, messageId: String?) {
        launchUI(strategy) {
            view.disableSendMessageButton()
            try {
                // ignore message for now, will receive it on the stream
                val message = if (messageId == null) {
                    client.sendMessage(chatRoomId, text)
                } else {
                    client.updateMessage(chatRoomId, messageId, text)
                }
                view.clearMessageComposition()
            } catch (ex: Exception) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                   view.showGenericErrorMessage()
                }
            } finally {
                view.enableSendMessageButton()
            }
        }
    }

    fun selectFile() {
        view.showFileSelection(settings.uploadMimeTypeFilter())
    }

    fun uploadFile(roomId: String, uri: Uri, msg: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val fileName = async { uriInteractor.getFileName(uri) }.await()
                val mimeType = async { uriInteractor.getMimeType(uri) }.await()
                val fileSize = async { uriInteractor.getFileSize(uri) }.await()
                val maxFileSize = settings.uploadMaxFileSize()

                when {
                    fileName.isNullOrEmpty() -> view.showInvalidFileMessage()
                    fileSize > maxFileSize -> view.showInvalidFileSize(fileSize, maxFileSize)
                    else -> {
                        Timber.d("Uploading to $roomId: $fileName - $mimeType")
                        client.uploadFile(roomId, fileName!!, mimeType, msg, description = fileName) {
                            uriInteractor.getInputStream(uri)
                        }
                    }
                }
            } catch (ex: RocketChatException) {
                Timber.d(ex)
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

    fun markRoomAsRead(roomId: String) {
        launchUI(strategy) {
            try {
                client.markAsRead(roomId)
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    private fun subscribeMessages(roomId: String) {
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
                client.unsubscribe(subscriptionId)
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
            if (!permissions.allowedMessageDeleting()) {
                return@launchUI
            }
            //TODO: Default delete message always to true. Until we have the permissions system
            //implemented, a user will only be able to delete his own messages.
            try {
                client.deleteMessage(roomId, id, true)
                // if Message_ShowDeletedStatus == true an update to that message will be dispatched.
                // Otherwise we signalize that we just want the message removed.
                if (!permissions.showDeletedStatus()) {
                    view.dispatchDeleteMessage(id)
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Quote or reply a message.
     *
     * @param roomType The current room type.
     * @param roomName The name of the current room.
     * @param messageId The id of the message to make citation for.
     * @param mentionAuthor true means the citation is a reply otherwise it's a quote.
     */
    fun citeMessage(roomType: String, roomName: String, messageId: String, mentionAuthor: Boolean) {
        launchUI(strategy) {
            val message = messagesRepository.getById(messageId)
            val me = client.me() //TODO: Cache this and use an interactor
            val serverUrl = serverInteractor.get()!!
            message?.let { m ->
                val id = m.id
                val username = m.sender?.username
                val user = "@" + if (settings.useRealName()) m.sender?.name
                        ?: m.sender?.username else m.sender?.username
                val mention = if (mentionAuthor && me.username != username) user else ""
                val type = roomTypeOf(roomType)
                val room = when (type) {
                    is RoomType.Channel -> "channel"
                    is RoomType.DirectMessage -> "direct"
                    is RoomType.PrivateGroup -> "group"
                    is RoomType.Livechat -> "livechat"
                    is RoomType.Custom -> "custom" //TODO: put appropriate callback string here.
                }
                view.showReplyingAction(
                        username = user,
                        replyMarkdown = "[ ]($serverUrl/$room/$roomName?msg=$id) $mention ",
                        quotedMessage = m.message
                )
            }
        }
    }

    /**
     * Copy message to clipboard.
     *
     * @param messageId The id of the message to copy to clipboard.
     */
    fun copyMessage(messageId: String) {
        launchUI(strategy) {
            try {
                messagesRepository.getById(messageId)?.let { m ->
                    view.copyToClipboard(m.message)
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Update message identified by given id with given text.
     *
     * @param roomId The id of the room of the message.
     * @param messageId The id of the message to update.
     * @param text The updated text.
     */
    fun editMessage(roomId: String, messageId: String, text: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessageEditing()) {
                view.showMessage(R.string.permission_editing_not_allowed)
                return@launchUI
            }
            view.showEditingAction(roomId, messageId, text)
        }
    }

    fun pinMessage(messageId: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessagePinning()) {
                view.showMessage(R.string.permission_pinning_not_allowed)
                return@launchUI
            }
            try {
                client.pinMessage(messageId)
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun unpinMessage(messageId: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessagePinning()) {
                view.showMessage(R.string.permission_pinning_not_allowed)
                return@launchUI
            }
            try {
                client.unpinMessage(messageId)
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
            val viewModelStreamedMessage = mapper.map(streamedMessage)
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
