package chat.rocket.android.chatroom.presentation

import android.net.Uri
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.AutoCompleteType
import chat.rocket.android.chatroom.adapter.PEOPLE
import chat.rocket.android.chatroom.adapter.ROOMS
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.chatroom.viewmodel.ViewModelMapper
import chat.rocket.android.chatroom.viewmodel.suggestion.ChatRoomSuggestionViewModel
import chat.rocket.android.chatroom.viewmodel.suggestion.CommandSuggestionViewModel
import chat.rocket.android.chatroom.viewmodel.suggestion.PeopleSuggestionViewModel
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.username
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.state
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.SimpleUser
import chat.rocket.common.model.UserStatus
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Command
import chat.rocket.core.model.Message
import chat.rocket.core.model.Myself
import chat.rocket.core.model.Value
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(private val view: ChatRoomView,
                                            private val navigator: ChatRoomNavigator,
                                            private val strategy: CancelStrategy,
                                            getSettingsInteractor: GetSettingsInteractor,
                                            serverInteractor: GetCurrentServerInteractor,
                                            private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                            private val permissions: GetPermissionsInteractor,
                                            private val uriInteractor: UriInteractor,
                                            private val messagesRepository: MessagesRepository,
                                            private val usersRepository: UsersRepository,
                                            private val roomsRepository: RoomRepository,
                                            private val localRepository: LocalRepository,
                                            factory: ConnectionManagerFactory,
                                            private val mapper: ViewModelMapper,
                                            private val jobSchedulerInteractor: JobSchedulerInteractor) {
    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)
    private val messagesChannel = Channel<Message>()

    private var chatRoomId: String? = null
    private var chatRoomType: String? = null
    private val stateChannel = Channel<State>()
    private var lastState = manager.state

    fun loadMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        this.chatRoomId = chatRoomId
        this.chatRoomType = chatRoomType
        launchUI(strategy) {
            view.showLoading()
            try {
                if (offset == 0L) {
                    val localMessages = messagesRepository.getByRoomId(chatRoomId)
                    val oldMessages = mapper.map(localMessages)
                    if (oldMessages.isNotEmpty()) {
                        view.showMessages(oldMessages)
                        loadMissingMessages()
                    } else {
                        loadAndShowMessages(chatRoomId, chatRoomType, offset)
                    }
                } else {
                    loadAndShowMessages(chatRoomId, chatRoomType, offset)
                }

                // TODO: For now we are marking the room as read if we can get the messages (I mean, no exception occurs)
                // but should mark only when the user see the first unread message.
                markRoomAsRead(chatRoomId)

                subscribeMessages(chatRoomId)
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

            if (offset == 0L) {
                subscribeState()
            }
        }
    }

    private suspend fun loadAndShowMessages(chatRoomId: String, chatRoomType: String, offset: Long = 0) {
        val messages =
            retryIO(description = "messages chatRoom: $chatRoomId, type: $chatRoomType, offset: $offset") {
                client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 30).result
            }
        messagesRepository.saveAll(messages)
        val allMessages = mapper.map(messages)
        view.showMessages(allMessages)
    }

    fun sendMessage(chatRoomId: String, text: String, messageId: String?) {
        launchUI(strategy) {
            try {
                // ignore message for now, will receive it on the stream
                val id = UUID.randomUUID().toString()
                val message = if (messageId == null) {
                    val username = localRepository.username()
                    val newMessage = Message(
                        id = id,
                        roomId = chatRoomId,
                        message = text,
                        timestamp = Instant.now().toEpochMilli(),
                        sender = SimpleUser(null, username, username),
                        attachments = null,
                        avatar = currentServer.avatarUrl(username!!),
                        channels = null,
                        editedAt = null,
                        editedBy = null,
                        groupable = false,
                        parseUrls = false,
                        pinned = false,
                        mentions = emptyList(),
                        reactions = null,
                        senderAlias = null,
                        type = null,
                        updatedAt = null,
                        urls = null,
                        isTemporary = true
                    )
                    messagesRepository.save(newMessage)
                    view.showNewMessage(mapper.map(newMessage))
                    client.sendMessage(id, chatRoomId, text)
                } else {
                    client.updateMessage(chatRoomId, messageId, text)
                }

                view.enableSendMessageButton()
            } catch (ex: Exception) {
                Timber.d(ex, "Error sending message...")
                jobSchedulerInteractor.scheduleSendingMessages()
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
                val fileName = async { uriInteractor.getFileName(uri) }.await() ?: uri.toString()
                val mimeType = async { uriInteractor.getMimeType(uri) }.await()
                val fileSize = async { uriInteractor.getFileSize(uri) }.await()
                val maxFileSize = settings.uploadMaxFileSize()

                when {
                    fileName.isNullOrEmpty() -> view.showInvalidFileMessage()
                    fileSize > maxFileSize -> view.showInvalidFileSize(fileSize, maxFileSize)
                    else -> {
                        Timber.d("Uploading to $roomId: $fileName - $mimeType")
                        retryIO("uploadFile($roomId, $fileName, $mimeType") {
                            client.uploadFile(roomId, fileName!!, mimeType, msg, description = fileName) {
                                uriInteractor.getInputStream(uri)
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error uploading file")
                when(ex) {
                    is RocketChatException -> view.showMessage(ex)
                    else -> view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    private fun markRoomAsRead(roomId: String) {
        launchUI(strategy) {
            try {
                retryIO(description = "markAsRead($roomId)") { client.markAsRead(roomId) }
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    private fun subscribeState() {
        Timber.d("Subscribing to Status changes")
        lastState = manager.state
        manager.addStatusChannel(stateChannel)
        launch(CommonPool + strategy.jobs) {
            for (state in stateChannel) {
                Timber.d("Got new state: $state - last: $lastState")
                if (state != lastState) {
                    launch(UI) {
                        view.showConnectionState(state)
                    }

                    if (state is State.Connected) {
                        jobSchedulerInteractor.scheduleSendingMessages()
                        loadMissingMessages()
                    }
                }
                lastState = state
            }
        }
    }

    private fun subscribeMessages(roomId: String) {
        manager.subscribeRoomMessages(roomId, messagesChannel)

        launch(CommonPool + strategy.jobs) {
            for (message in messagesChannel) {
                Timber.d("New message for room ${message.roomId}")
                updateMessage(message)
            }
        }
    }

    private fun loadMissingMessages() {
        launch(parent = strategy.jobs) {
            if (chatRoomId != null && chatRoomType != null) {
                val roomType = roomTypeOf(chatRoomType!!)
                messagesRepository.getByRoomId(chatRoomId!!)
                    .sortedByDescending { it.timestamp }.firstOrNull()?.let { lastMessage ->
                        val instant = Instant.ofEpochMilli(lastMessage.timestamp).toString()
                        try {
                            val messages = retryIO(description = "history($chatRoomId, $roomType, $instant)") {
                                client.history(chatRoomId!!, roomType, count = 50,
                                    oldest = instant)
                            }
                            Timber.d("History: $messages")

                            if (messages.result.isNotEmpty()) {
                                val models = mapper.map(messages.result)
                                messagesRepository.saveAll(messages.result)

                                launchUI(strategy) {
                                    view.showNewMessage(models)
                                }

                                if (messages.result.size == 50) {
                                    // we loaded at least count messages, try one more to fetch more messages
                                    loadMissingMessages()
                                }
                            }
                        } catch (ex: Exception) {
                            // TODO - we need to better treat connection problems here, but no let gaps
                            // on the messages list
                            Timber.d(ex, "Error fetching channel history")
                            ex.printStackTrace()
                        }
                    }
            }
        }
    }

    fun unsubscribeMessages(chatRoomId: String) {
        manager.removeStatusChannel(stateChannel)
        manager.unsubscribeRoomMessages(chatRoomId)
        // All messages during the subscribed period are assumed to be read,
        // and lastSeen is updated as the time when the user leaves the room
        markRoomAsRead(chatRoomId)
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
                retryIO(description = "deleteMessage($roomId, $id)") {
                    client.deleteMessage(roomId, id, true)
                }
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
            val me: Myself? = try {
                retryIO("me()") { client.me() } //TODO: Cache this and use an interactor
            } catch (ex: Exception) {
                Timber.d(ex, "Error getting myself info.")
                ex.printStackTrace()
                null
            }
            message?.let { m ->
                val id = m.id
                val username = m.sender?.username
                val user = "@" + if (settings.useRealName()) m.sender?.name
                    ?: m.sender?.username else m.sender?.username
                val mention = if (mentionAuthor && me?.username != username) user else ""
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
                    replyMarkdown = "[ ]($currentServer/$room/$roomName?msg=$id) $mention ",
                    quotedMessage = mapper.map(message).last().preview?.message ?: ""
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
                retryIO("pinMessage($messageId)") { client.pinMessage(messageId) }
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
                retryIO("unpinMessage($messageId)") { client.unpinMessage(messageId) }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun loadActiveMembers(chatRoomId: String, chatRoomType: String, offset: Long = 0, filterSelfOut: Boolean = false) {
        launchUI(strategy) {
            try {
                val members = retryIO("getMembers($chatRoomId, $chatRoomType, $offset)") {
                    client.getMembers(chatRoomId, roomTypeOf(chatRoomType), offset, 50).result
                }
                usersRepository.saveAll(members)
                val self = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                // Take at most the 100 most recent messages distinguished by user. Can return less.
                val recentMessages = messagesRepository.getRecentMessages(chatRoomId, 100)
                    .filterNot { filterSelfOut && it.sender?.username == self }
                val activeUsers = mutableListOf<PeopleSuggestionViewModel>()
                recentMessages.forEach {
                    val sender = it.sender!!
                    val username = sender.username ?: ""
                    val name = sender.name ?: ""
                    val avatarUrl = currentServer.avatarUrl(username)
                    val found = members.firstOrNull { member -> member.username == username }
                    val status = if (found != null) found.status else UserStatus.Offline()
                    val searchList = mutableListOf(username, name)
                    activeUsers.add(PeopleSuggestionViewModel(avatarUrl, username, username, name, status,
                        true, searchList))
                }
                // Filter out from members list the active users.
                val others = members.filterNot { member ->
                    activeUsers.firstOrNull {
                        it.username == member.username
                    } != null
                }
                // Add found room members who're not active enough and add them in without pinning.
                activeUsers.addAll(others.map {
                    val username = it.username ?: ""
                    val name = it.name ?: ""
                    val avatarUrl = currentServer.avatarUrl(username)
                    val searchList = mutableListOf(username, name)
                    PeopleSuggestionViewModel(avatarUrl, username, username, name, it.status, true, searchList)
                })

                view.populatePeopleSuggestions(activeUsers)
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun spotlight(query: String, @AutoCompleteType type: Int, filterSelfOut: Boolean = false) {
        launchUI(strategy) {
            try {
                val (users, rooms) = retryIO("spotlight($query)") { client.spotlight(query) }
                when (type) {
                    PEOPLE -> {
                        if (users.isNotEmpty()) {
                            usersRepository.saveAll(users)
                        }
                        val self = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                        view.populatePeopleSuggestions(users.map {
                            val username = it.username ?: ""
                            val name = it.name ?: ""
                            val searchList = mutableListOf(username, name)
                            it.emails?.forEach { email -> searchList.add(email.address) }
                            PeopleSuggestionViewModel(currentServer.avatarUrl(username),
                                username, username, name, it.status, false, searchList)
                        }.filterNot { filterSelfOut && self != null && self == it.text })
                    }
                    ROOMS -> {
                        if (rooms.isNotEmpty()) {
                            roomsRepository.saveAll(rooms)
                        }
                        view.populateRoomSuggestions(rooms.map {
                            val fullName = it.fullName ?: ""
                            val name = it.name ?: ""
                            val searchList = mutableListOf(fullName, name)
                            ChatRoomSuggestionViewModel(name, fullName, name, searchList)
                        })
                    }
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun toMembersList(chatRoomId: String, chatRoomType: String) = navigator.toMembersList(chatRoomId, chatRoomType)

    fun loadChatRooms() {
        launchUI(strategy) {
            try {
                val chatRooms = getChatRoomsInteractor.get(currentServer)
                    .filterNot {
                        it.type is RoomType.DirectMessage || it.type is RoomType.Livechat
                    }
                    .map { chatRoom ->
                        val name = chatRoom.name
                        val fullName = chatRoom.fullName ?: ""
                        ChatRoomSuggestionViewModel(
                            text = name,
                            name = name,
                            fullName = fullName,
                            searchList = listOf(name, fullName)
                        )
                    }
                view.populateRoomSuggestions(chatRooms)
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun joinChat(chatRoomId: String) {
        launchUI(strategy) {
            try {
                retryIO("joinChat($chatRoomId)") { client.joinChat(chatRoomId) }
                view.onJoined()
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Send an emoji reaction to a message.
     */
    fun react(messageId: String, emoji: String) {
        launchUI(strategy) {
            try {
                retryIO("toogleEmoji($messageId, $emoji)") {
                    client.toggleReaction(messageId, emoji.removeSurrounding(":"))
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    fun showReactions(messageId: String) {
        view.showReactionsPopup(messageId)
    }

    fun loadCommands() {
        launchUI(strategy) {
            try {
                //TODO: cache the commands
                val commands = retryIO("commands(0, 100)") {
                    client.commands(0, 100).result
                }
                view.populateCommandSuggestions(commands.map {
                    CommandSuggestionViewModel(it.command, it.description ?: "", listOf(it.command))
                })
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    fun runCommand(text: String, roomId: String) {
        launchUI(strategy) {
            try {
                if (text.length == 1) {
                    view.disableSendMessageButton()
                    // we have just the slash, post it anyway
                    sendMessage(roomId, text, null)
                } else {
                    view.disableSendMessageButton()
                    val command = text.split(" ")
                    val name = command[0].substring(1)
                    var params = ""
                    command.forEachIndexed { index, param ->
                        if (index > 0) {
                            params += "$param "
                        }
                    }
                    val result = retryIO("runCommand($name, $params, $roomId)") {
                        client.runCommand(Command(name, params), roomId)
                    }
                    if (!result) {
                        // failed, command is not valid so post it
                        sendMessage(roomId, text, null)
                    }
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                // command is not valid, post it
                sendMessage(roomId, text, null)
            } finally {
                view.enableSendMessageButton()
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
