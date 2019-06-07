package chat.rocket.android.chatroom.presentation

import android.graphics.Bitmap
import android.net.Uri
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.SubscriptionTypeEvent
import chat.rocket.android.chatroom.adapter.AutoCompleteType
import chat.rocket.android.chatroom.adapter.PEOPLE
import chat.rocket.android.chatroom.adapter.ROOMS
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.chatroom.uimodel.RoomUiModel
import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.EmojiSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.chatrooms.adapter.RoomUiModelMapper
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.helper.MessageHelper
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.JobSchedulerInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.UsersRepository
import chat.rocket.android.server.domain.uploadMaxFileSize
import chat.rocket.android.server.domain.uploadMimeTypeFilter
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.state
import chat.rocket.android.util.extension.getByteArray
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryDB
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
import chat.rocket.core.internal.rest.getMembers
import chat.rocket.core.internal.rest.history
import chat.rocket.core.internal.rest.joinChat
import chat.rocket.core.internal.rest.markAsRead
import chat.rocket.core.internal.rest.messages
import chat.rocket.core.internal.rest.pinMessage
import chat.rocket.core.internal.rest.reportMessage
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
import chat.rocket.core.model.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import timber.log.Timber
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
    private val localRepository: LocalRepository,
    private val analyticsManager: AnalyticsManager,
    private val userHelper: UserHelper,
    private val mapper: UiModelMapper,
    private val roomMapper: RoomUiModelMapper,
    private val jobSchedulerInteractor: JobSchedulerInteractor,
    private val messageHelper: MessageHelper,
    private val dbManager: DatabaseManager,
    tokenRepository: TokenRepository,
    getSettingsInteractor: GetSettingsInteractor,
    serverInteractor: GetCurrentServerInteractor,
    factory: ConnectionManagerFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private var settings: PublicSettings = getSettingsInteractor.get(serverInteractor.get()!!)
    private val token = tokenRepository.get(currentServer)
    private val currentLoggedUsername = userHelper.username()
    private val messagesChannel = Channel<Message>()

    private var chatRoomId: String? = null
    private lateinit var chatRoomType: String
    private lateinit var chatRoomName: String
    private var isBroadcast: Boolean = false
    private var chatRoles = emptyList<ChatRoomRole>()
    private val stateChannel = Channel<State>()
    private var typingStatusSubscriptionId: String? = null
    private var lastState = manager.state
    private var typingStatusList = arrayListOf<String>()
    private val roomChangesChannel = Channel<Room>(Channel.CONFLATED)
    private var lastMessageId: String? = null
    private lateinit var draftKey: String

    fun setupChatRoom(
        roomId: String,
        roomName: String,
        roomType: String,
        chatRoomMessage: String? = null
    ) {
        draftKey = "${currentServer}_${LocalRepository.DRAFT_KEY}$roomId"
        chatRoomId = roomId
        chatRoomType = roomType
        chatRoomName = roomName
        chatRoles = emptyList()
        var canModerate = isOwnerOrMod()

        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            // Can post anyway if has the 'post-readonly' permission on server.
            val room = dbManager.getRoom(roomId)
            room?.let {
                isBroadcast = it.chatRoom.broadcast ?: false
                val roomUiModel = roomMapper.map(it, true)
                launchUI(strategy) {
                    view.onRoomUpdated(
                        roomUiModel = roomUiModel.copy(
                            broadcast = isBroadcast,
                            canModerate = canModerate,
                            writable = roomUiModel.writable || canModerate
                        )
                    )
                }
            }

            loadMessages(roomId, chatRoomType, clearDataSet = true)
            loadActiveMembers(roomId, chatRoomType, filterSelfOut = true)

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


            /*FIXME:  Get chat role can cause unresponsive problems especially on slower connections
                      We are updating the room again after the first step so that initial messages
                      get loaded in and the system appears more responsive.  Something should be
                      done to either fix the load in speed of moderator roles or store the
                      information locally*/
            if (getChatRole()) {
                canModerate = isOwnerOrMod()
                if (canModerate) {
                    //FIXME: add this in when moderator page is actually created
                    //view.updateModeration()
                }
            }

            subscribeRoomChanges()
        }
    }

    private suspend fun getChatRole(): Boolean {
        try {
            if (roomTypeOf(chatRoomType) !is RoomType.DirectMessage) {
                chatRoles = withContext(Dispatchers.IO + strategy.jobs) {
                    client.chatRoomRoles(
                        roomType = roomTypeOf(chatRoomType),
                        roomName = chatRoomName
                    )
                }
                return true
            } else {
                chatRoles = emptyList()
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            chatRoles = emptyList()
        }
        return false
    }

    private suspend fun subscribeRoomChanges() {
        withContext(Dispatchers.IO + strategy.jobs) {
            chatRoomId?.let {
                manager.addRoomChannel(it, roomChangesChannel)
                for (room in roomChangesChannel) {
                    dbManager.getRoom(room.id)?.let { chatRoom ->
                        view.onRoomUpdated(
                            roomMapper.map(
                                chatRoom = chatRoom,
                                showLastMessage = true
                            )
                        )
                    }
                }
            }
        }
    }

    private fun unsubscribeRoomChanges() {
        chatRoomId?.let { manager.removeRoomChannel(it) }
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

        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            try {
                if (offset == 0L) {
                    // FIXME - load just 50 messages from DB to speed up. We will reload from Network after that
                    // FIXME - We need to handle the pagination, first fetch from DB, then from network
                    val localMessages = messagesRepository.getRecentMessages(chatRoomId, 50)
                    val oldMessages = mapper.map(
                        localMessages, RoomUiModel(
                            roles = chatRoles,
                            // FIXME: Why are we fixing isRoom attribute to true here?
                            isBroadcast = isBroadcast, isRoom = true
                        )
                    )
                    lastMessageId = localMessages.firstOrNull()?.id
                    val lastSyncDate = messagesRepository.getLastSyncDate(chatRoomId)
                    if (oldMessages.isNotEmpty() && lastSyncDate != null) {
                        view.showMessages(oldMessages, clearDataSet)
                        loadMissingMessages()
                    } else {
                        loadAndShowMessages(chatRoomId, chatRoomType, offset, clearDataSet)
                    }
                } else {
                    loadAndShowMessages(chatRoomId, chatRoomType, offset, clearDataSet)
                }

                // TODO: For now we are marking the room as read if we can get the messages (I mean, no exception occurs)
                // but should mark only when the user sees the first unread message.
                markRoomAsRead(chatRoomId)

                subscribeMessages(chatRoomId)
            } catch (ex: Exception) {
                Timber.e(ex)
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
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

        //we are saving last sync date of latest synced chat room message
        if (offset == 0L) {
            //if success - saving last synced time
            if (messages.isEmpty()) {
                //chat history is empty - just saving current date
                messagesRepository.saveLastSyncDate(chatRoomId, System.currentTimeMillis())
            } else {
                //assume that BE returns ordered messages, the first message is the latest one
                messagesRepository.saveLastSyncDate(chatRoomId, messages.first().timestamp)
            }
        }

        view.showMessages(
            mapper.map(
                messages,
                RoomUiModel(roles = chatRoles, isBroadcast = isBroadcast, isRoom = true)
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
                        RoomUiModel(chatRoles, isBroadcast, true)
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
                view.disableSendMessageButton()
                // ignore message for now, will receive it on the stream
                if (messageId == null) {
                    val id = UUID.randomUUID().toString()
                    val username = userHelper.username()
                    val user = userHelper.user()
                    val timestamp = maxOf(getTimeStampOfLastMessageInRoom() + 1,
                        Instant.now().toEpochMilli())
                    val newMessage = Message(
                        id = id,
                        roomId = chatRoomId,
                        message = text,
                        timestamp = timestamp,
                        sender = SimpleUser(user?.id, user?.username ?: username, user?.name),
                        attachments = null,
                        avatar = currentServer.avatarUrl(
                            username!!,
                            token?.userId,
                            token?.authToken
                        ),
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
                        synced = false,
                        unread = true
                    )
                    try {
                        messagesRepository.save(newMessage)
                        view.showNewMessage(
                            mapper.map(
                                newMessage,
                                RoomUiModel(roles = chatRoles, isBroadcast = isBroadcast)
                            ), false
                        )
                        client.sendMessage(id, chatRoomId, text)
                        messagesRepository.save(newMessage.copy(synced = true))
                        logMessageSent()
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
                    lastMessageId = id
                } else {
                    client.updateMessage(chatRoomId, messageId, text)
                }
                clearDraftMessage()
            } catch (ex: Exception) {
                Timber.e(ex, "Error sending message...")
                jobSchedulerInteractor.scheduleSendingMessages()
            } finally {
                view.clearMessageComposition(true)
                view.enableSendMessageButton()
            }
        }
    }

    fun reportMessage(messageId: String, description: String) {
        launchUI(strategy) {
            try {
                retryIO("reportMessage($messageId, $description)") {
                    client.reportMessage(messageId = messageId, description = description)
                }
                view.showMessage(R.string.report_sent)
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    fun selectFile() {
        view.showFileSelection(settings.uploadMimeTypeFilter())
    }

    fun uploadImage(roomId: String, mimeType: String, uri: Uri, bitmap: Bitmap, msg: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(Dispatchers.Default) {
                    val fileName = uriInteractor.getFileName(uri) ?: uri.toString()
                    if (fileName.isEmpty()) {
                        view.showInvalidFileMessage()
                    } else {
                        val byteArray =
                            bitmap.getByteArray(mimeType, 100, settings.uploadMaxFileSize())
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

                        logMediaUploaded(mimeType)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error uploading image")
                when (ex) {
                    is RocketChatException -> view.showMessage(ex)
                    else -> view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun uploadFile(roomId: String, mimeType: String, uri: Uri, msg: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(Dispatchers.Default) {
                    val fileName = uriInteractor.getFileName(uri) ?: uri.toString()
                    val fileSize = uriInteractor.getFileSize(uri)
                    val maxFileSizeAllowed = settings.uploadMaxFileSize()

                    when {
                        fileName.isEmpty() -> view.showInvalidFileMessage()
                        fileSize > maxFileSizeAllowed && maxFileSizeAllowed !in -1..0 ->
                            view.showInvalidFileSize(fileSize, maxFileSizeAllowed)
                        else -> {
                            retryIO("uploadFile($roomId, $fileName, $mimeType") {
                                client.uploadFile(
                                    roomId,
                                    fileName,
                                    mimeType,
                                    msg,
                                    description = fileName
                                ) {
                                    uriInteractor.getInputStream(uri)
                                }
                            }
                            logMediaUploaded(mimeType)
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
                withContext(Dispatchers.Default) {
                    val fileName = UUID.randomUUID().toString() + ".png"
                    val fileSize = byteArray.size
                    val mimeType = "image/png"
                    val maxFileSizeAllowed = settings.uploadMaxFileSize()

                    when {
                        fileSize > maxFileSizeAllowed && maxFileSizeAllowed !in -1..0 ->
                            view.showInvalidFileSize(fileSize, maxFileSizeAllowed)
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

    fun sendTyping() {
        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            if (chatRoomId != null && currentLoggedUsername != null) {
                client.setTypingStatus(chatRoomId.toString(), currentLoggedUsername, true)
            }
        }
    }

    fun sendNotTyping() {
        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
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
        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            for (state in stateChannel) {
                Timber.d("Got new state: $state - last: $lastState")
                if (state != lastState) {
                    launch(Dispatchers.Main) {
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

        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            for (message in messagesChannel) {
                Timber.d("New message for room ${message.roomId}")
                updateMessage(message)
            }
        }
    }

    private fun loadMissingMessages() {
        GlobalScope.launch(strategy.jobs) {
            chatRoomId?.let { chatRoomId ->
                val roomType = roomTypeOf(chatRoomType)
                val lastSyncDate = messagesRepository.getLastSyncDate(chatRoomId)
                // lastSyncDate or 0. LastSyncDate could be in case when we sent some messages offline(and saved them locally),
                // but never has obtained chatMessages(or history) from remote. In this case we should sync all chat history from beginning
                val instant = Instant.ofEpochMilli(lastSyncDate ?: 0).toString()
                //
                try {
                    val messages =
                        retryIO(description = "history($chatRoomId, $roomType, $instant)") {
                            client.history(
                                chatRoomId, roomType, count = 50,
                                oldest = instant
                            )
                        }
                    Timber.d("History: $messages")

                    if (messages.result.isNotEmpty()) {
                        val models = mapper.map(
                            messages.result, RoomUiModel(
                                roles = chatRoles,
                                isBroadcast = isBroadcast,
                                // FIXME: Why are we fixing isRoom attribute to true here?
                                isRoom = true
                            )
                        )
                        messagesRepository.saveAll(messages.result)
                        //if success - saving last synced time
                        //assume that BE returns ordered messages, the first message is the latest one
                        messagesRepository.saveLastSyncDate(
                            chatRoomId,
                            messages.result.first().timestamp
                        )

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
                            isBroadcast = isBroadcast
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
                    view.showMessage(R.string.msg_message_copied)
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

    suspend fun loadActiveMembers(
        chatRoomId: String,
        chatRoomType: String,
        offset: Long = 0,
        filterSelfOut: Boolean = false
    ) {
        val activeUsers = mutableListOf<PeopleSuggestionUiModel>()

        withContext(Dispatchers.IO + strategy.jobs) {
            try {
                val members = retryIO("getMembers($chatRoomId, $chatRoomType, $offset)") {
                    client.getMembers(chatRoomId, roomTypeOf(chatRoomType), offset, 50).result
                }.take(50) // Get only 50, the backend is returning 7k+ users
                usersRepository.saveAll(members)
                dbManager.processUsersBatch(members)
                val self = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                // Take at most the 100 most recent messages distinguished by user. Can return less.
                val recentMessages = messagesRepository.getRecentMessages(chatRoomId, 100)
                    .filterNot { filterSelfOut && it.sender?.username == self }
                recentMessages.forEach {
                    val sender = it.sender
                    val username = sender?.username ?: ""
                    val name = sender?.name ?: ""
                    val avatarUrl =
                        currentServer.avatarUrl(username, token?.userId, token?.authToken)
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
                    val avatarUrl =
                        currentServer.avatarUrl(username, token?.userId, token?.authToken)
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

            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
        launchUI(strategy) {
            view.populatePeopleSuggestions(activeUsers)
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
                                currentServer.avatarUrl(username, token?.userId, token?.authToken),
                                username, username, name, it.status, false, searchList
                            )
                        }.filterNot { filterSelfOut && self != null && self == it.text })
                    }
                    ROOMS -> {
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

    fun toChatDetails(
        chatRoomId: String,
        chatRoomType: String,
        isSubscribed: Boolean,
        isFavorite: Boolean,
        isMenuDisabled: Boolean
    ) {
        navigator.toChatDetails(chatRoomId, chatRoomType, isSubscribed, isFavorite, isMenuDisabled)
    }

    fun loadChatRoomsSuggestions() {
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
    private suspend fun getChatRoomAsync(roomId: String): ChatRoom? = withContext(Dispatchers.IO) {
        retryDB("getRoom($roomId)") {
            dbManager.chatRoomDao().getSync(roomId)?.let {
                with(it.chatRoom) {
                    ChatRoom(
                        id = id,
                        subscriptionId = subscriptionId,
                        parentId = parentId,
                        type = roomTypeOf(type),
                        unread = unread,
                        broadcast = broadcast ?: false,
                        alert = alert,
                        fullName = fullname,
                        name = name,
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
    }

    // TODO: move this to new interactor or FetchChatRoomsInteractor?
    private suspend fun getChatRoomsAsync(name: String? = null): List<ChatRoom> =
        withContext(Dispatchers.IO) {
            retryDB("getAllSync()") {
                dbManager.chatRoomDao().getAllSync().filter {
                    if (name == null) {
                        return@filter true
                    }
                    it.chatRoom.name == name || it.chatRoom.fullname == name
                }.map {
                    with(it.chatRoom) {
                        ChatRoom(
                            id = id,
                            subscriptionId = subscriptionId,
                            parentId = parentId,
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
        }

    fun joinChat(chatRoomId: String) {
        launchUI(strategy) {
            try {
                retryIO("joinChat($chatRoomId)") { client.joinChat(chatRoomId) }
                val canPost = permissions.canPostToReadOnlyChannels()
                dbManager.getRoom(chatRoomId)?.let {
                    val roomUiModel = roomMapper.map(it, true).copy(
                        writable = canPost
                    )
                    view.onJoined(roomUiModel = roomUiModel)
                    view.onRoomUpdated(roomUiModel = roomUiModel)
                }
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
            } catch (exception: Exception) {
                Timber.e(exception)
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun copyPermalink(messageId: String) {
        launchUI(strategy) {
            try {
                messagesRepository.getById(messageId)?.let { message ->
                    getChatRoomAsync(message.roomId)?.let {
                        val models = mapper.map(message)
                        models.firstOrNull()?.permalink?.let {
                            view.copyToClipboard(it)
                            view.showMessage(R.string.msg_permalink_copied)
                        }
                    }
                }
            } catch (exception: Exception) {
                Timber.e(exception)
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
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
                logReactionEvent()
            } catch (exception: RocketChatException) {
                Timber.e(exception)
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun reactToLastMessage(text: String, roomId: String) {
        launchUI(strategy) {
            lastMessageId?.let { messageId ->
                val emoji = text.substring(1).trimEnd()
                if (emoji.length >= 2 && emoji.startsWith(":") && emoji.endsWith(":")) {
                    try {
                        retryIO("toggleEmoji($messageId, $emoji)") {
                            client.toggleReaction(messageId, emoji.removeSurrounding(":"))
                        }
                        logReactionEvent()
                        view.clearMessageComposition(true)
                    } catch (ex: RocketChatException) {
                        Timber.e(ex)
                        // emoji is not valid, post it
                        sendMessage(roomId, text, null)
                    }
                } else {
                    sendMessage(roomId, text, null)
                }
            }.ifNull {
                sendMessage(roomId, text, null)
            }
        }
    }

    private fun logReactionEvent() {
        when {
            roomTypeOf(chatRoomType) is RoomType.DirectMessage ->
                analyticsManager.logReaction(SubscriptionTypeEvent.DirectMessage)
            roomTypeOf(chatRoomType) is RoomType.Channel ->
                analyticsManager.logReaction(SubscriptionTypeEvent.Channel)
            else -> analyticsManager.logReaction(SubscriptionTypeEvent.Group)
        }
    }

    private fun logMediaUploaded(mimeType: String) {
        when {
            roomTypeOf(chatRoomType) is RoomType.DirectMessage ->
                analyticsManager.logMediaUploaded(SubscriptionTypeEvent.DirectMessage, mimeType)
            roomTypeOf(chatRoomType) is RoomType.Channel ->
                analyticsManager.logMediaUploaded(SubscriptionTypeEvent.Channel, mimeType)
            else -> analyticsManager.logMediaUploaded(SubscriptionTypeEvent.Group, mimeType)
        }
    }

    private fun logMessageSent() {
        when {
            roomTypeOf(chatRoomType) is RoomType.DirectMessage ->
                analyticsManager.logMessageSent(SubscriptionTypeEvent.DirectMessage)
            roomTypeOf(chatRoomType) is RoomType.Channel ->
                analyticsManager.logMessageSent(SubscriptionTypeEvent.Channel)
            else -> analyticsManager.logMessageSent(SubscriptionTypeEvent.Group)
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

    fun loadEmojis() {
        launchUI(strategy) {
            val emojiSuggestionUiModels = EmojiRepository.getAll().map {
                EmojiSuggestionUiModel(
                    text = it.shortname.replaceFirst(":", ""),
                    pinned = false,
                    emoji = it,
                    searchList = listOf(it.shortname)
                )
            }
            view.populateEmojiSuggestions(emojis = emojiSuggestionUiModels)
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
                    val index = text.indexOf(" ")
                    var name = ""
                    var params = ""
                    if (index >= 1) {
                        name = text.substring(1, index)
                        params = text.substring(index + 1).trim()
                    }
                    val result = retryIO("runCommand($name, $params, $roomId)") {
                        client.runCommand(Command(name, params), roomId)
                    }
                    if (result) {
                        view.clearMessageComposition(true)
                    } else {
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
        unsubscribeRoomChanges()
        unsubscribeTypingStatus()
        if (chatRoomId != null) {
            unsubscribeMessages(chatRoomId.toString())
        }
    }

    private fun subscribeTypingStatus() {
        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            client.subscribeTypingStatus(chatRoomId.toString()) { _, id ->
                typingStatusSubscriptionId = id
            }

            for (typingStatus in client.typingStatusChannel) {
                processTypingStatus(typingStatus)
            }
        }
    }

    private fun processTypingStatus(typingStatus: Pair<String, Boolean>) {
        synchronized(typingStatusList) {
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
                    roles = chatRoles, isBroadcast = isBroadcast, isRoom = true
                )
            )
            val roomMessages = messagesRepository.getByRoomId(streamedMessage.roomId)
            val index = roomMessages.indexOfFirst { msg -> msg.id == streamedMessage.id }
            if (index > -1) {
                Timber.d("Updating message at $index")
                //messagesRepository.save(streamedMessage)
                view.dispatchUpdateMessage(index, viewModelStreamedMessage)
            } else {
                Timber.d("Adding new message")
                //messagesRepository.save(streamedMessage)
                view.showNewMessage(viewModelStreamedMessage, true)
            }
        }
    }

    fun messageInfo(messageId: String) {
        launchUI(strategy) {
            navigator.toMessageInformation(messageId = messageId)
        }
    }

    /**
     * Save unfinished message, when user left chat room without sending a message.
     *
     * @param unfinishedMessage The unfinished message to save.
     */
    fun saveDraftMessage(unfinishedMessage: String) {
        localRepository.save(draftKey, unfinishedMessage)
    }

    fun clearDraftMessage() {
        localRepository.clear(draftKey)
    }

    /**
     * Get unfinished message from local repository, when user left chat room without
     * sending a message and now the user is back.
     *
     * @return Returns the unfinished message, null otherwise.
     */
    fun getDraftUnfinishedMessage(): String? {
        return localRepository.get(draftKey)
    }

    private suspend fun getTimeStampOfLastMessageInRoom(): Long {
        return withContext(Dispatchers.IO + strategy.jobs) {
            chatRoomId?.let {
                dbManager.messageDao().getRecentMessagesByRoomId(it, 1).first().message.message.timestamp
            }
        } ?: 0
    }

    fun openFullWebPage(roomId: String, url: String){
        navigator.toFullWebPage(roomId, url)
    }

    fun openConfigurableWebPage(roomId: String, url: String, heightRatio: String){
        navigator.toConfigurableWebPage(roomId, url, heightRatio)
    }
}
