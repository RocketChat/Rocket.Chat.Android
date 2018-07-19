package chat.rocket.android.chatroom.presentation

import android.net.Uri
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.AutoCompleteType
import chat.rocket.android.chatroom.adapter.PEOPLE
import chat.rocket.android.chatroom.adapter.ROOMS
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.chatroom.uimodel.RoomUiModel
import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.MessageHelper
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.JobSchedulerInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.RoomRepository
import chat.rocket.android.server.domain.UsersRepository
import chat.rocket.android.server.domain.uploadMaxFileSize
import chat.rocket.android.server.domain.uploadMimeTypeFilter
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.state
import chat.rocket.android.util.extension.compressImageAndGetInputStream
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.SimpleUser
import chat.rocket.common.model.UserStatus
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.setTypingStatus
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.realtime.subscribeTypingStatus
import chat.rocket.core.internal.realtime.unsubscribe
import chat.rocket.core.internal.rest.chatRoomRoles
import chat.rocket.core.internal.rest.commands
import chat.rocket.core.internal.rest.deleteMessage
import chat.rocket.core.internal.rest.favorite
import chat.rocket.core.internal.rest.getMembers
import chat.rocket.core.internal.rest.history
import chat.rocket.core.internal.rest.joinChat
import chat.rocket.core.internal.rest.markAsRead
import chat.rocket.core.internal.rest.messages
import chat.rocket.core.internal.rest.pinMessage
import chat.rocket.core.internal.rest.runCommand
import chat.rocket.core.internal.rest.searchMessages
import chat.rocket.core.internal.rest.sendMessage
import chat.rocket.core.internal.rest.spotlight
import chat.rocket.core.internal.rest.starMessage
import chat.rocket.core.internal.rest.toggleReaction
import chat.rocket.core.internal.rest.unpinMessage
import chat.rocket.core.internal.rest.unstarMessage
import chat.rocket.core.internal.rest.updateMessage
import chat.rocket.core.internal.rest.uploadFile
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.ChatRoomRole
import chat.rocket.core.model.Command
import chat.rocket.core.model.Message
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.Instant
import timber.log.Timber
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class ChatRoomPresenter @Inject constructor(
    private val view: ChatRoomView,
    private val navigator: ChatRoomNavigator,
    private val strategy: CancelStrategy,
    private val permissions: PermissionsInteractor,
    private val uriInteractor: UriInteractor,
    private val messagesRepository: MessagesRepository,
    private val usersRepository: UsersRepository,
    private val roomsRepository: RoomRepository,
    private val localRepository: LocalRepository,
    private val userHelper: UserHelper,
    private val mapper: UiModelMapper,
    private val jobSchedulerInteractor: JobSchedulerInteractor,
    private val messageHelper: MessageHelper,
    private val dbManager: DatabaseManager,
    getSettingsInteractor: GetSettingsInteractor,
    serverInteractor: GetCurrentServerInteractor,
    factory: ConnectionManagerFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private var settings: PublicSettings = getSettingsInteractor.get(serverInteractor.get()!!)
    private val currentLoggedUsername = userHelper.username()
    private val messagesChannel = Channel<Message>()

    private var chatRoomId: String? = null
    private var chatRoomType: String? = null
    private var chatIsBroadcast: Boolean = false
    private var chatRoles = emptyList<ChatRoomRole>()
    private val stateChannel = Channel<State>()
    private var typingStatusSubscriptionId: String? = null
    private var lastState = manager.state
    private var typingStatusList = arrayListOf<String>()

    fun setupChatRoom(
        roomId: String,
        roomName: String,
        roomType: String,
        chatRoomMessage: String? = null
    ) {
        launchUI(strategy) {
            try {
                chatRoles = if (roomTypeOf(roomType) !is RoomType.DirectMessage) {
                    client.chatRoomRoles(roomType = roomTypeOf(roomType), roomName = roomName)
                } else emptyList()
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                chatRoles = emptyList()
            } finally {
                // User has at least an 'owner' or 'moderator' role.
                val userCanMod = isOwnerOrMod()
                // Can post anyway if has the 'post-readonly' permission on server.
                val userCanPost = userCanMod || permissions.canPostToReadOnlyChannels()
                chatIsBroadcast = dbManager.getRoom(roomId)?.chatRoom?.run {
                    broadcast
                } ?: false
                view.onRoomUpdated(userCanPost, chatIsBroadcast, userCanMod)
                loadMessages(roomId, roomType)
                chatRoomMessage?.let { messageHelper.messageIdFromPermalink(it) }
                    ?.let { messageId ->
                        val name = messageHelper.roomNameFromPermalink(chatRoomMessage)
                        citeMessage(
                            name!!,
                            messageHelper.roomTypeFromPermalink(chatRoomMessage)!!,
                            messageId,
                            true
                        )
                    }
            }
        }
    }

    private fun isOwnerOrMod(): Boolean {
        return chatRoles.firstOrNull { it.user.username == currentLoggedUsername }?.roles?.any {
            it == "owner" || it == "moderator"
        } ?: false
    }

    fun loadMessages(
        chatRoomId: String,
        chatRoomType: String,
        offset: Long = 0,
        clearDataSet: Boolean = false
    ) {
        this.chatRoomId = chatRoomId
        this.chatRoomType = chatRoomType
        launchUI(strategy) {
            view.showLoading()
            try {
                if (offset == 0L) {
                    val localMessages = messagesRepository.getByRoomId(chatRoomId)
                    val oldMessages = mapper.map(
                        localMessages, RoomUiModel(
                            roles = chatRoles,
                            // FIXME: Why are we fixing isRoom attribute to true here?
                            isBroadcast = chatIsBroadcast, isRoom = true
                        )
                    )
                    if (oldMessages.isNotEmpty()) {
                        view.showMessages(oldMessages, clearDataSet)
                        loadMissingMessages()
                    } else {
                        loadAndShowMessages(chatRoomId, chatRoomType, offset, clearDataSet)
                    }
                } else {
                    loadAndShowMessages(chatRoomId, chatRoomType, offset, clearDataSet)
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

            subscribeTypingStatus()
            subscribeState()
        }
    }

    private suspend fun loadAndShowMessages(
        chatRoomId: String,
        chatRoomType: String,
        offset: Long = 0,
        clearDataSet: Boolean
    ) {
        val messages =
            retryIO("loadAndShowMessages($chatRoomId, $chatRoomType, $offset") {
                client.messages(chatRoomId, roomTypeOf(chatRoomType), offset, 30).result
            }
        messagesRepository.saveAll(messages)
        view.showMessages(
            mapper.map(
                messages,
                RoomUiModel(roles = chatRoles, isBroadcast = chatIsBroadcast, isRoom = true)
            ),
            clearDataSet
        )
    }

    fun searchMessages(chatRoomId: String, searchText: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                val messages = retryIO("searchMessages($chatRoomId, $searchText)") {
                    client.searchMessages(chatRoomId, searchText).result
                }
                view.showSearchedMessages(
                    mapper.map(
                        messages,
                        RoomUiModel(chatRoles, chatIsBroadcast, true)
                    )
                )
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

    fun sendMessage(chatRoomId: String, text: String, messageId: String?) {
        launchUI(strategy) {
            try {
                // ignore message for now, will receive it on the stream
                val id = UUID.randomUUID().toString()
                val message = if (messageId == null) {
                    val username = userHelper.username()
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
                        starred = emptyList(),
                        mentions = emptyList(),
                        reactions = null,
                        senderAlias = null,
                        type = null,
                        updatedAt = null,
                        urls = null,
                        isTemporary = true,
                        unread = true
                    )
                    try {
                        messagesRepository.save(newMessage)
                        view.showNewMessage(
                            mapper.map(
                                newMessage, 
                                RoomUiModel(roles = chatRoles, isBroadcast = chatIsBroadcast)
                            ), false
                        )
                        client.sendMessage(id, chatRoomId, text)
                    } catch (ex: Exception) {
                        // Ok, not very beautiful, but the backend sends us a not valid response
                        // When someone sends a message on a read-only channel, so we just ignore it
                        // and show a generic error message
                        // TODO - remove the generic message when we implement :userId:/message subscription
                        if (ex is IllegalStateException) {
                            Timber.d(ex, "Probably a read-only problem...")
                            view.showGenericErrorMessage()
                        } else {
                            // some other error, just rethrow it...
                            throw ex
                        }
                    }
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
                withContext(DefaultDispatcher) {
                    val fileName = uriInteractor.getFileName(uri) ?: uri.toString()
                    val fileSize = uriInteractor.getFileSize(uri)
                    val mimeType = uriInteractor.getMimeType(uri)
                    val maxFileSizeAllowed = settings.uploadMaxFileSize()

                    when {
                        fileName.isEmpty() -> {
                            view.showInvalidFileMessage()
                        }
                        fileSize > maxFileSizeAllowed -> {
                            view.showInvalidFileSize(fileSize, maxFileSizeAllowed)
                        }
                        else -> {
                            var inputStream: InputStream? = uriInteractor.getInputStream(uri)

                            if (mimeType.contains("image")) {
                                uriInteractor.getBitmap(uri)?.let {
                                    it.compressImageAndGetInputStream(mimeType)?.let {
                                        inputStream = it
                                    }
                                }
                            }

                            retryIO("uploadFile($roomId, $fileName, $mimeType") {
                                client.uploadFile(
                                    roomId,
                                    fileName,
                                    mimeType,
                                    msg,
                                    description = fileName
                                ) {
                                    inputStream
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error uploading file")
                when (ex) {
                    is RocketChatException -> view.showMessage(ex)
                    else -> view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun uploadDrawingImage(roomId: String, byteArray: ByteArray, msg: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {
                    val fileName = UUID.randomUUID().toString() + ".png"
                    val fileSize = byteArray.size
                    val mimeType = "image/png"
                    val maxFileSizeAllowed = settings.uploadMaxFileSize()

                    when {
                        fileSize > maxFileSizeAllowed -> {
                            view.showInvalidFileSize(fileSize, maxFileSizeAllowed)
                        }
                        else -> {
                            retryIO("uploadFile($roomId, $fileName, $mimeType") {
                                client.uploadFile(
                                    roomId,
                                    fileName,
                                    mimeType,
                                    msg,
                                    description = fileName
                                ) {
                                    byteArray.inputStream()
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error uploading file")
                when (ex) {
                    is RocketChatException -> view.showMessage(ex)
                    else -> view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun sendTyping() {
        launch(CommonPool + strategy.jobs) {
            if (chatRoomId != null && currentLoggedUsername != null) {
                client.setTypingStatus(chatRoomId.toString(), currentLoggedUsername, true)
            }
        }
    }

    fun sendNotTyping() {
        launch(CommonPool + strategy.jobs) {
            if (chatRoomId != null && currentLoggedUsername != null) {
                client.setTypingStatus(chatRoomId.toString(), currentLoggedUsername, false)
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

    private suspend fun subscribeState() {
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
                            val messages =
                                retryIO(description = "history($chatRoomId, $roomType, $instant)") {
                                    client.history(
                                        chatRoomId!!, roomType, count = 50,
                                        oldest = instant
                                    )
                                }
                            Timber.d("History: $messages")

                            if (messages.result.isNotEmpty()) {
                                val models = mapper.map(messages.result, RoomUiModel(
                                    roles = chatRoles,
                                    isBroadcast = chatIsBroadcast,
                                    // FIXME: Why are we fixing isRoom attribute to true here?
                                    isRoom = true
                                ))
                                messagesRepository.saveAll(messages.result)

                                launchUI(strategy) {
                                    view.showNewMessage(models, true)
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
                        }
                    }
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
     * @param messageId The id of the message to make citation for.
     * @param mentionAuthor true means the citation is a reply otherwise it's a quote.
     */
    fun citeMessage(roomName: String, roomType: String, messageId: String, mentionAuthor: Boolean) {
        launchUI(strategy) {
            val message = messagesRepository.getById(messageId)
            val currentUsername: String? = userHelper.user()?.username
            message?.let { msg ->
                val id = msg.id
                val username = msg.sender?.username ?: ""
                val mention = if (mentionAuthor && currentUsername != username) "@$username" else ""
                val room =
                    if (roomTypeOf(roomType) is RoomType.DirectMessage) username else roomName
                val chatRoomType = when (roomTypeOf(roomType)) {
                    is RoomType.DirectMessage -> "direct"
                    is RoomType.PrivateGroup -> "group"
                    is RoomType.Channel -> "channel"
                    is RoomType.LiveChat -> "livechat"
                    else -> "custom"
                }
                view.showReplyingAction(
                    username = getDisplayName(msg.sender),
                    replyMarkdown = "[ ]($currentServer/$chatRoomType/$room?msg=$id) $mention ",
                    quotedMessage = mapper.map(
                        message, RoomUiModel(
                            roles = chatRoles,
                            isBroadcast = chatIsBroadcast
                        )
                    ).last().preview?.message ?: ""
                )
            }
        }
    }

    private fun getDisplayName(user: SimpleUser?): String {
        val username = user?.username ?: ""
        return if (settings.useRealName()) user?.name ?: "@$username" else "@$username"
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

    fun starMessage(messageId: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessageStarring()) {
                view.showMessage(R.string.permission_starring_not_allowed)
                return@launchUI
            }
            try {
                retryIO("starMessage($messageId)") { client.starMessage(messageId) }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun unstarMessage(messageId: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessageStarring()) {
                view.showMessage(R.string.permission_starring_not_allowed)
                return@launchUI
            }
            try {
                retryIO("unstarMessage($messageId)") { client.unstarMessage(messageId) }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
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

    fun loadActiveMembers(
        chatRoomId: String,
        chatRoomType: String,
        offset: Long = 0,
        filterSelfOut: Boolean = false
    ) {
        launchUI(strategy) {
            try {
                val members = retryIO("getMembers($chatRoomId, $chatRoomType, $offset)") {
                    client.getMembers(chatRoomId, roomTypeOf(chatRoomType), offset, 50).result
                }.take(50) // Get only 50, the backend is returning 7k+ users
                usersRepository.saveAll(members)
                val self = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                // Take at most the 100 most recent messages distinguished by user. Can return less.
                val recentMessages = messagesRepository.getRecentMessages(chatRoomId, 100)
                    .filterNot { filterSelfOut && it.sender?.username == self }
                val activeUsers = mutableListOf<PeopleSuggestionUiModel>()
                recentMessages.forEach {
                    val sender = it.sender!!
                    val username = sender.username ?: ""
                    val name = sender.name ?: ""
                    val avatarUrl = currentServer.avatarUrl(username)
                    val found = members.firstOrNull { member -> member.username == username }
                    val status = if (found != null) found.status else UserStatus.Offline()
                    val searchList = mutableListOf(username, name)
                    activeUsers.add(
                        PeopleSuggestionUiModel(
                            avatarUrl, username, username, name, status,
                            true, searchList
                        )
                    )
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
                    PeopleSuggestionUiModel(
                        avatarUrl,
                        username,
                        username,
                        name,
                        it.status,
                        true,
                        searchList
                    )
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
                            PeopleSuggestionUiModel(
                                currentServer.avatarUrl(username),
                                username, username, name, it.status, false, searchList
                            )
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
                            ChatRoomSuggestionUiModel(name, fullName, name, searchList)
                        })
                    }
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun toggleFavoriteChatRoom(roomId: String, isFavorite: Boolean) {
        launchUI(strategy) {
            try {
                // Note that if it is favorite then the user wants to unfavorite - and vice versa.
                retryIO("favorite($roomId, $isFavorite)") {
                    client.favorite(roomId, !isFavorite)
                }
                view.showFavoriteIcon(!isFavorite)
            } catch (e: RocketChatException) {
                Timber.e(e, "Error while trying to favorite/unfavorite chat room.")
                e.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun toMembersList(chatRoomId: String) =
        navigator.toMembersList(chatRoomId)

    fun toMentions(chatRoomId: String) =
        navigator.toMentions(chatRoomId)

    fun toPinnedMessageList(chatRoomId: String) =
        navigator.toPinnedMessageList(chatRoomId)

    fun toFavoriteMessageList(chatRoomId: String) =
        navigator.toFavoriteMessageList(chatRoomId)

    fun toFileList(chatRoomId: String) =
        navigator.toFileList(chatRoomId)

    fun loadChatRooms() {
        launchUI(strategy) {
            try {
                val chatRooms = getChatRoomsAsync()
                    .filterNot {
                        it.type is RoomType.DirectMessage || it.type is RoomType.LiveChat
                    }
                    .map { chatRoom ->
                        val name = chatRoom.name
                        val fullName = chatRoom.fullName ?: ""
                        ChatRoomSuggestionUiModel(
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

    // TODO: move this to new interactor or FetchChatRoomsInteractor?
    private suspend fun getChatRoomsAsync(name: String? = null): List<ChatRoom> = withContext(CommonPool) {
        return@withContext dbManager.chatRoomDao().getAllSync().filter {
            if (name == null) {
                return@filter true
            }
            it.chatRoom.name == name || it.chatRoom.fullname == name
        }.map {
            with (it.chatRoom) {
                ChatRoom(
                    id = id,
                    subscriptionId = subscriptionId,
                    type = roomTypeOf(type),
                    unread = unread,
                    broadcast = broadcast ?: false,
                    alert = alert,
                    fullName = fullname,
                    name = name ?: "",
                    favorite = favorite ?: false,
                    default = isDefault ?: false,
                    readonly = readonly,
                    open = open,
                    lastMessage = null,
                    archived = false,
                    status = null,
                    user = null,
                    userMentions = userMentions,
                    client = client,
                    announcement = null,
                    description = null,
                    groupMentions = groupMentions,
                    roles = null,
                    topic = null,
                    lastSeen = this.lastSeen,
                    timestamp = timestamp,
                    updatedAt = updatedAt
                )
            }
        }
    }

    fun joinChat(chatRoomId: String) {
        launchUI(strategy) {
            try {
                retryIO("joinChat($chatRoomId)") { client.joinChat(chatRoomId) }
                val canPost = permissions.canPostToReadOnlyChannels()
                view.onJoined(canPost)
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    fun openDirectMessage(roomName: String, message: String) {
        launchUI(strategy) {
            try {
                getChatRoomsAsync(roomName).let {
                    if (it.isNotEmpty()) {
                        if (it.first().type is RoomType.DirectMessage) {
                            navigator.toDirectMessage(
                                chatRoomId = it.first().id,
                                chatRoomType = it.first().type.toString(),
                                chatRoomLastSeen = it.first().lastSeen ?: -1,
                                chatRoomName = roomName,
                                isChatRoomCreator = false,
                                isChatRoomFavorite = false,
                                isChatRoomReadOnly = false,
                                isChatRoomSubscribed = it.first().open,
                                chatRoomMessage = message
                            )
                        } else {
                            throw IllegalStateException("Not a direct-message")
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                view.showMessage(ex.message!!)
            }
        }
    }

    /**
     * Send an emoji reaction to a message.
     */
    fun react(messageId: String, emoji: String) {
        launchUI(strategy) {
            try {
                retryIO("toggleEmoji($messageId, $emoji)") {
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
                    CommandSuggestionUiModel(it.command, it.description ?: "", listOf(it.command))
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

    fun disconnect() {
        unsubscribeTypingStatus()
        if (chatRoomId != null) {
            unsubscribeMessages(chatRoomId.toString())
        }
    }

    private fun subscribeTypingStatus() {
        launch(CommonPool + strategy.jobs) {
            client.subscribeTypingStatus(chatRoomId.toString()) { _, id ->
                typingStatusSubscriptionId = id
            }

            for (typingStatus in client.typingStatusChannel) {
                processTypingStatus(typingStatus)
            }
        }
    }

    private fun processTypingStatus(typingStatus: Pair<String, Boolean>) {
        if (typingStatus.first != currentLoggedUsername) {
            if (!typingStatusList.any { username -> username == typingStatus.first }) {
                if (typingStatus.second) {
                    typingStatusList.add(typingStatus.first)
                }
            } else {
                typingStatusList.find { username -> username == typingStatus.first }?.let {
                    typingStatusList.remove(it)
                    if (typingStatus.second) {
                        typingStatusList.add(typingStatus.first)
                    }
                }
            }

            if (typingStatusList.isNotEmpty()) {
                view.showTypingStatus(typingStatusList.toList())
            } else {
                view.hideTypingStatusView()
            }
        }
    }

    private fun unsubscribeTypingStatus() {
        typingStatusSubscriptionId?.let {
            client.unsubscribe(it)
        }
    }

    private fun unsubscribeMessages(chatRoomId: String) {
        manager.removeStatusChannel(stateChannel)
        manager.unsubscribeRoomMessages(chatRoomId)
        // All messages during the subscribed period are assumed to be read,
        // and lastSeen is updated as the time when the user leaves the room
        markRoomAsRead(chatRoomId)
    }

    private fun updateMessage(streamedMessage: Message) {
        launchUI(strategy) {
            val viewModelStreamedMessage = mapper.map(
                streamedMessage, RoomUiModel(
                    roles = chatRoles, isBroadcast = chatIsBroadcast, isRoom = true
                )
            )

            val roomMessages = messagesRepository.getByRoomId(streamedMessage.roomId)
            val index = roomMessages.indexOfFirst { msg -> msg.id == streamedMessage.id }
            if (index > -1) {
                Timber.d("Updating message at $index")
                messagesRepository.save(streamedMessage)
                view.dispatchUpdateMessage(index, viewModelStreamedMessage)
            } else {
                Timber.d("Adding new message")
                messagesRepository.save(streamedMessage)
                view.showNewMessage(viewModelStreamedMessage, true)
            }
        }
    }

    fun messageInfo(messageId: String) {
        launchUI(strategy) {
            navigator.toMessageInformation(messageId = messageId)
        }
    }
}