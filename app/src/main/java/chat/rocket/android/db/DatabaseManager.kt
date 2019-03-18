package chat.rocket.android.db

import android.app.Application
import chat.rocket.android.R
import chat.rocket.android.db.model.BaseMessageEntity
import chat.rocket.android.db.model.BaseUserEntity
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.MessageChannels
import chat.rocket.android.db.model.MessageEntity
import chat.rocket.android.db.model.MessageFavoritesRelation
import chat.rocket.android.db.model.MessageMentionsRelation
import chat.rocket.android.db.model.MessagesSync
import chat.rocket.android.db.model.ReactionEntity
import chat.rocket.android.db.model.UrlEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.db.model.UserStatus
import chat.rocket.android.db.model.asEntity
import chat.rocket.android.util.extensions.exhaustive
import chat.rocket.android.util.extensions.removeTrailingSlash
import chat.rocket.android.util.extensions.toEntity
import chat.rocket.android.util.extensions.userId
import chat.rocket.android.util.retryDB
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.SimpleUser
import chat.rocket.common.model.User
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.socket.model.StreamMessage
import chat.rocket.core.internal.realtime.socket.model.Type
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Message
import chat.rocket.core.model.Myself
import chat.rocket.core.model.Room
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.userId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.HashSet
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.system.measureTimeMillis

class DatabaseManager(val context: Application, val serverUrl: String) {

    private val database: RCDatabase = androidx.room.Room.databaseBuilder(
        context,
        RCDatabase::class.java, serverUrl.databaseName()
    )
        .fallbackToDestructiveMigration()
        .build()
    private val dbContext = newSingleThreadContext("$serverUrl-db-context")
    private val dbManagerContext = newSingleThreadContext("$serverUrl-db-manager-context")

    private val writeChannel = Channel<Operation>(Channel.UNLIMITED)
    private var dbJob: Job? = null

    private val insertSubs = HashMap<String, Subscription>()
    private val insertRooms = HashMap<String, Room>()
    private val updateSubs = LinkedHashMap<String, Subscription>()
    private val updateRooms = LinkedHashMap<String, Room>()

    fun chatRoomDao(): ChatRoomDao = database.chatRoomDao()
    fun userDao(): UserDao = database.userDao()
    fun messageDao(): MessageDao = database.messageDao()

    init {
        start()
    }

    fun start() {
        dbJob?.cancel()
        dbJob = GlobalScope.launch(dbContext) {
            for (operation in writeChannel) {
                doOperation(operation)
            }
        }
    }

    fun stop() {
        dbJob?.cancel()
        dbJob = null
    }

    suspend fun sendOperation(operation: Operation) {
        Timber.d("writerChannel: $writeChannel, closedForSend: ${writeChannel.isClosedForSend}, closedForReceive: ${writeChannel.isClosedForReceive}, empty: ${writeChannel.isEmpty}, full: ${writeChannel.isFull}")
        writeChannel.send(operation)
    }

    suspend fun clearUsersStatus() {
        withContext(dbManagerContext) {
            sendOperation(Operation.ClearStatus)
        }
    }

    suspend fun logout() {
        retryDB("clearAllTables") { database.clearAllTables() }
    }

    suspend fun getRoom(id: String) = withContext(dbManagerContext) {
        retryDB("getRoom($id)") {
            chatRoomDao().getSync(id)
        }
    }

    suspend fun insertOrReplaceRoom(chatRoomEntity: ChatRoomEntity) {
        retryDB("insertOrReplace($chatRoomEntity)") {
            chatRoomDao().insertOrReplace(chatRoomEntity)
        }
    }

    suspend fun getUser(id: String) = withContext(dbManagerContext) {
        retryDB("getUser($id)") {
            userDao().getUser(id)
        }
    }

    fun processUsersBatch(users: List<User>) {
        GlobalScope.launch(dbManagerContext) {
            val list = ArrayList<BaseUserEntity>(users.size)
            val time = measureTimeMillis {
                users.forEach { user ->
                    user.toEntity()?.let { entity ->
                        list.add(entity)
                    }
                }
            }
            Timber.d("Converted users batch(${users.size}) in $time MS")
            sendOperation(Operation.InsertUsers(list))
        }
    }

    /*
     * Creates a list of data base operations
     */
    fun processChatRoomsBatch(batch: List<StreamMessage<BaseRoom>>) {
        GlobalScope.launch(dbManagerContext) {
            val toRemove = HashSet<String>()
            val toInsert = ArrayList<ChatRoomEntity>(batch.size / 2)
            val toUpdate = ArrayList<ChatRoomEntity>(batch.size)
            batch.forEach {
                when (it.type) {
                    is Type.Removed -> toRemove.add(removeChatRoom(it.data))
                    is Type.Inserted -> insertChatRoom(it.data)?.let { room -> toInsert.add(room) }
                    is Type.Updated -> {
                        when (it.data) {
                            is Subscription -> updateSubs[(it.data as Subscription).roomId] =
                                it.data as Subscription
                            is Room -> updateRooms[(it.data as Room).id] = it.data as Room
                        }
                    }
                }
            }

            toUpdate.addAll(createMatchingUpdates())
            toUpdate.addAll(createUpdates())

            try {
                val filteredUpdate = toUpdate.filterNot { toRemove.contains(it.id) }
                val filteredInsert = toInsert.filterNot { toRemove.contains(it.id) }

                sendOperation(
                    Operation.UpdateRooms(
                        filteredInsert,
                        filteredUpdate,
                        toRemove.toList()
                    )
                )
            } catch (ex: Exception) {
                Timber.d(ex, "Error updating chatrooms")
            }
        }
    }

    fun updateSelfUser(myself: Myself) {
        GlobalScope.launch(dbManagerContext) {
            val user = retryDB("getUser(${myself.id})") { userDao().getUser(myself.id) }
            val entity = user?.copy(
                name = myself.name ?: user.name,
                username = myself.username ?: user.username,
                utcOffset = myself.utcOffset ?: user.utcOffset,
                status = myself.status?.toString() ?: user.status
            ) ?: myself.asUser().toEntity()

            Timber.d("UPDATING SELF: $entity")
            entity?.let { sendOperation(Operation.UpsertUser(it)) }
        }
    }

    fun processRooms(rooms: List<ChatRoom>) {
        GlobalScope.launch(dbManagerContext) {
            val entities = rooms.map { mapChatRoom(it) }
            sendOperation(Operation.CleanInsertRooms(entities))
        }
    }

    fun processMessagesBatch(messages: List<Message>): Job = GlobalScope.launch(dbManagerContext) {
        val list = mutableListOf<Pair<MessageEntity, List<BaseMessageEntity>>>()
        messages.forEach { message ->
            val pair = createMessageEntities(message)
            list.add(pair)
        }
        sendOperation(Operation.InsertMessages(list))
    }

    private suspend fun createMessageEntities(message: Message): Pair<MessageEntity, List<BaseMessageEntity>> {
        val messageEntity = message.toEntity()
        val list = mutableListOf<BaseMessageEntity>()
        createAttachments(message)?.let { list.addAll(it) }
        createFavoriteRelations(message)?.let { list.addAll(it) }
        createMentionRelations(message)?.let { list.addAll(it) }
        createChannelRelations(message)?.let { list.addAll(it) }
        createUrlEntities(message)?.let { list.addAll(it) }
        createReactions(message)?.let { list.addAll(it) }

        insertUserIfMissing(message.sender)
        insertUserIfMissing(message.editedBy)
        return Pair(messageEntity, list)
    }

    private fun createReactions(message: Message): List<BaseMessageEntity>? =
        message.reactions?.run {
            if (isNotEmpty()) {
                val list = mutableListOf<BaseMessageEntity>()
                keys.forEach { reaction ->
                    get(reaction)?.let { reactionValue ->
                        list.add(
                            ReactionEntity(
                                reaction,
                                message.id,
                                size,
                                reactionValue.joinToString()
                            )
                        )
                    }
                }
                list
            } else null
        }

    private fun createUrlEntities(message: Message): List<BaseMessageEntity>? = message.urls?.run {
        if (isNotEmpty()) {
            val list = mutableListOf<UrlEntity>()
            forEach { url ->
                list.add(
                    UrlEntity(
                        message.id, url.url, url.parsedUrl?.host, url.meta?.title,
                        url.meta?.description, url.meta?.imageUrl
                    )
                )
            }
            list
        } else null
    }

    private fun createChannelRelations(message: Message): List<BaseMessageEntity>? =
        message.channels?.run {
            if (isNotEmpty()) {
                val list = mutableListOf<MessageChannels>()
                forEach { channel ->
                    list.add(MessageChannels(message.id, channel.id, channel.name))
                }
                list
            } else null
        }

    private suspend fun createMentionRelations(message: Message): List<BaseMessageEntity>? =
        message.mentions?.run {
            if (isNotEmpty()) {
                val list = mutableListOf<MessageMentionsRelation>()
                filterNot { user -> user.id.isNullOrEmpty() }.forEach { mention ->
                    insertUserIfMissing(mention)
                    list.add(MessageMentionsRelation(message.id, mention.id!!))
                }
                list
            } else null
        }

    private suspend fun createFavoriteRelations(message: Message): List<BaseMessageEntity>? =
        message.starred?.run {
            if (isNotEmpty()) {
                val list = mutableListOf<MessageFavoritesRelation>()
                filterNot { user -> user.id.isNullOrEmpty() }.forEach { userId ->
                    insertUserIfMissing(userId)
                    list.add(MessageFavoritesRelation(message.id, userId.id!!))
                }
                list
            } else null
        }


    private fun createAttachments(message: Message): List<BaseMessageEntity>? =
        message.attachments?.run {
            if (isNotEmpty()) {
                val list = ArrayList<BaseMessageEntity>(size)
                forEach { attachment ->
                    list.addAll(attachment.asEntity(message.id, context))
                }
                list
            } else null
        }

    private suspend fun createUpdates(): List<ChatRoomEntity> {
        val list = ArrayList<ChatRoomEntity>()

        updateSubs.forEach { (_, subscription) ->
            updateSubscription(subscription)?.let {
                list.add(it)
            }
        }

        updateRooms.forEach { (_, room) ->
            updateRoom(room)?.let {
                list.add(it)
            }
        }

        updateSubs.clear()
        updateRooms.clear()

        return list
    }

    private suspend fun createMatchingUpdates(): List<ChatRoomEntity> {
        val list = ArrayList<ChatRoomEntity>()
        val matches = ArrayList<String>()

        updateRooms.forEach { room ->
            val (id, _) = room
            if (updateSubs.containsKey(id)) {
                matches.add(id)
            }
        }

        matches.forEach { id ->
            val room = updateRooms.remove(id)
            val subscription = updateSubs.remove(id)

            list.add(fullChatRoomEntity(subscription!!, room!!))
        }

        return list
    }

    private fun removeChatRoom(data: BaseRoom): String {
        return when (data) {
            is Subscription -> data.roomId
            else -> data.id
        }
    }

    private suspend fun updateRoom(data: Room): ChatRoomEntity? {
        return retryDB("getChatRoom(${data.id})") { chatRoomDao().getSync(data.id) }?.let { current ->
            with(data) {
                val chatRoom = current.chatRoom

                insertUserIfMissing(lastMessage?.sender)
                insertUserIfMissing(user)

                chatRoom.copy(
                    name = name ?: chatRoom.name,
                    fullname = fullName ?: chatRoom.fullname,
                    ownerId = user?.id ?: chatRoom.ownerId,
                    readonly = readonly,
                    updatedAt = updatedAt ?: chatRoom.updatedAt,
                    topic = topic,
                    announcement = announcement,
                    description = description,
                    lastMessageText = mapLastMessageText(lastMessage),
                    lastMessageUserId = lastMessage?.sender?.id,
                    lastMessageTimestamp = lastMessage?.timestamp,
                    muted = muted ?: chatRoom.muted
                )
            }
        }
    }

    private fun mapLastMessageText(message: Message?): String? = message?.run {
        if (this.message.isEmpty() && attachments?.isNotEmpty() == true) {
            message.attachments?.let { mapAttachmentText(it[0]) }
        } else {
            this.message
        }
    }

    private fun mapAttachmentText(attachment: Attachment): String =
        context.getString(R.string.msg_sent_attachment)

    private suspend fun updateSubscription(data: Subscription): ChatRoomEntity? {
        return retryDB("getRoom(${data.roomId}") { chatRoomDao().getSync(data.roomId) }?.let { current ->
            with(data) {

                val userId = if (type is RoomType.DirectMessage) {
                    roomId.userId(user?.id)
                } else {
                    null
                }

                insertUserIfMissing(userId)

                val chatRoom = current.chatRoom
                chatRoom.copy(
                    id = roomId,
                    subscriptionId = id,
                    type = type.toString(),
                    name = name
                        ?: throw NullPointerException(), // this should be filtered on the SDK
                    fullname = fullName ?: chatRoom.fullname,
                    userId = userId ?: chatRoom.userId,
                    readonly = readonly ?: chatRoom.readonly,
                    isDefault = isDefault,
                    favorite = isFavorite,
                    topic = chatRoom.topic,
                    announcement = chatRoom.announcement,
                    description = chatRoom.description,
                    open = open,
                    alert = alert,
                    unread = unread,
                    userMentions = userMentions ?: chatRoom.userMentions,
                    groupMentions = groupMentions ?: chatRoom.groupMentions,
                    updatedAt = updatedAt ?: chatRoom.updatedAt,
                    timestamp = timestamp ?: chatRoom.timestamp,
                    lastSeen = lastSeen ?: chatRoom.lastSeen,
                    muted = chatRoom.muted
                )
            }
        }
    }

    private suspend fun insertChatRoom(data: BaseRoom): ChatRoomEntity? = when (data) {
        is Room -> insertRoom(data)
        is Subscription -> insertSubscription(data)
        else -> null
    }

    private suspend fun insertRoom(data: Room): ChatRoomEntity? {
        val subscription = insertSubs.remove(data.id)
        return if (subscription != null) {
            fullChatRoomEntity(subscription, data)
        } else {
            insertRooms[data.id] = data
            null
        }
    }

    private suspend fun insertSubscription(data: Subscription): ChatRoomEntity? {
        val room = insertRooms.remove(data.roomId)
        return if (room != null) {
            fullChatRoomEntity(data, room)
        } else {
            insertSubs[data.roomId] = data
            null
        }
    }

    private suspend fun fullChatRoomEntity(subscription: Subscription, room: Room): ChatRoomEntity {
        val userId = if (room.type is RoomType.DirectMessage) {
            subscription.roomId.userId(subscription.user?.id)
        } else {
            null
        }

        insertUserIfMissing(userId)
        insertUserIfMissing(room.lastMessage?.sender)
        insertUserIfMissing(room.user)

        return ChatRoomEntity(
            id = room.id,
            subscriptionId = subscription.id,
            type = room.type.toString(),
            name = room.name ?: subscription.name
            ?: throw NullPointerException(), // this should be filtered on the SDK
            fullname = subscription.fullName ?: room.fullName,
            userId = userId,
            ownerId = room.user?.id,
            readonly = subscription.readonly,
            isDefault = subscription.isDefault,
            favorite = subscription.isFavorite,
            topic = room.topic,
            announcement = room.announcement,
            description = room.description,
            open = subscription.open,
            alert = subscription.alert,
            unread = subscription.unread,
            userMentions = subscription.userMentions,
            groupMentions = subscription.groupMentions,
            updatedAt = subscription.updatedAt,
            timestamp = subscription.timestamp,
            lastSeen = subscription.lastSeen,
            lastMessageText = mapLastMessageText(room.lastMessage),
            lastMessageUserId = room.lastMessage?.sender?.id,
            lastMessageTimestamp = room.lastMessage?.timestamp,
            broadcast = room.broadcast
        )
    }

    private suspend fun mapChatRoom(room: ChatRoom): ChatRoomEntity {
        with(room) {
            val userId = userId()
            if (userId != null && findUser(userId) == null) {
                Timber.d("Missing user, inserting: $userId")
                insert(UserEntity(userId))
            }

            insertUserIfMissing(lastMessage?.sender)
            insertUserIfMissing(user)

            return ChatRoomEntity(
                id = id,
                subscriptionId = subscriptionId,
                type = type.toString(),
                name = name,
                fullname = fullName,
                userId = userId,
                ownerId = user?.id,
                readonly = readonly,
                isDefault = default,
                favorite = favorite,
                topic = topic,
                announcement = announcement,
                description = description,
                open = open,
                alert = alert,
                unread = unread,
                userMentions = userMentions,
                groupMentions = groupMentions,
                updatedAt = updatedAt,
                timestamp = timestamp,
                lastSeen = lastSeen,
                lastMessageText = mapLastMessageText(lastMessage),
                lastMessageUserId = lastMessage?.sender?.id,
                lastMessageTimestamp = lastMessage?.timestamp,
                broadcast = broadcast,
                muted = room.muted
            )
        }
    }

    suspend fun insert(rooms: List<ChatRoomEntity>) {
        withContext(dbManagerContext) {
            sendOperation(Operation.CleanInsertRooms(rooms))
        }
    }

    suspend fun insert(user: UserEntity) {
        sendOperation(Operation.InsertUser(user))
    }

    private suspend fun insertUserIfMissing(id: String?) {
        if (id != null && findUser(id) == null) {
            Timber.d("Missing user, inserting: $id")
            sendOperation(Operation.InsertUser(UserEntity(id)))
        }
    }

    private suspend fun insertUserIfMissing(user: SimpleUser?) {
        if (user?.id != null && findUser(user.id!!) == null) {
            Timber.d("Missing user, inserting: ${user.id}")
            sendOperation(Operation.InsertUser(UserEntity(user.id!!, user.username, user.name)))
        }
    }

    private suspend fun findUser(userId: String): String? =
        retryDB("findUser($userId)") { userDao().findUser(userId) }

    private suspend fun doOperation(operation: Operation) {
        retryDB(description = "doOperation($operation)") {
            when (operation) {
                is Operation.ClearStatus -> userDao().clearStatus()
                is Operation.UpdateRooms -> {
                    Timber.d("Running ChatRooms transaction: remove: ${operation.toRemove} - insert: ${operation.toInsert} - update: ${operation.toUpdate}")
                    chatRoomDao().update(operation.toInsert, operation.toUpdate, operation.toRemove)
                }
                is Operation.InsertRooms -> chatRoomDao().insertOrReplace(operation.chatRooms)
                is Operation.CleanInsertRooms -> chatRoomDao().cleanInsert(operation.chatRooms)
                is Operation.InsertUsers -> {
                    val time = measureTimeMillis { userDao().upsert(operation.users) }
                    Timber.d("Upserted users batch(${operation.users.size}) in $time MS")
                }
                is Operation.InsertUser -> userDao().insert(operation.user)
                is Operation.UpsertUser -> userDao().upsert(operation.user)
                is Operation.InsertMessages -> messageDao().insert(operation.list)
                is Operation.SaveLastSync -> messageDao().saveLastSync(operation.sync)
            }.exhaustive
        }
    }
}

sealed class Operation {
    object ClearStatus : Operation()

    data class UpdateRooms(
        val toInsert: List<ChatRoomEntity>,
        val toUpdate: List<ChatRoomEntity>,
        val toRemove: List<String>
    ) : Operation()

    data class InsertRooms(val chatRooms: List<ChatRoomEntity>) : Operation()
    data class CleanInsertRooms(val chatRooms: List<ChatRoomEntity>) : Operation()

    data class InsertUsers(val users: List<BaseUserEntity>) : Operation()
    data class UpsertUser(val user: BaseUserEntity) : Operation()
    data class InsertUser(val user: UserEntity) : Operation()

    data class InsertMessages(val list: List<Pair<MessageEntity, List<BaseMessageEntity>>>) :
        Operation()

    data class SaveLastSync(val sync: MessagesSync) : Operation()
}

fun User.toEntity(): BaseUserEntity? {
    return if ((name == null || username == null) && status != null) {
        UserStatus(id = id, status = status.toString())
    } else if (username != null) {
        UserEntity(id, username, name, status?.toString() ?: "offline", utcOffset)
    } else {
        null
    }
}

private fun Myself.asUser(): User {
    return User(id, name, username, status, utcOffset, null, roles)
}

private fun String.databaseName(): String {
    val tmp = this.removePrefix("https://")
        .removePrefix("http://")
        .removeTrailingSlash()
        .replace("/", "-")
        .replace(".", "_")

    return "$tmp.db"
}