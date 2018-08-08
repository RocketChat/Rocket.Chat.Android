package chat.rocket.android.db

import android.app.Application
import androidx.room.migration.Migration
import chat.rocket.android.R
import chat.rocket.android.db.model.BaseUserEntity
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.db.model.UserStatus
import chat.rocket.android.util.extensions.removeTrailingSlash
import chat.rocket.android.util.extensions.userId
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.model.RoomType
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
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.util.HashSet

class DatabaseManager(val context: Application,
                      val serverUrl: String) {

    private val database: RCDatabase = androidx.room.Room.databaseBuilder(context,
            RCDatabase::class.java, serverUrl.databaseName())
            .addMigrations(RCDatabase.MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
    private val dbContext = newSingleThreadContext("$serverUrl-db-context")

    private val insertSubs = HashMap<String, Subscription>()
    private val insertRooms = HashMap<String, Room>()
    private val updateSubs = LinkedHashMap<String, Subscription>()
    private val updateRooms = LinkedHashMap<String, Room>()

    fun chatRoomDao(): ChatRoomDao = database.chatRoomDao()
    fun userDao(): UserDao = database.userDao()

    fun clearUsersStatus() {
        launch(dbContext) {
            userDao().clearStatus()
        }
    }

    fun logout() {
        database.clearAllTables()
    }

    suspend fun getRoom(id: String) = withContext(dbContext) {
        chatRoomDao().get(id)
    }

    fun processUsersBatch(users: List<User>) {
        launch(dbContext) {
            val dao = database.userDao()
            val list = ArrayList<BaseUserEntity>(users.size)
            users.forEach { user ->
                user.toEntity()?.let { entity ->
                    list.add(entity)
                }
            }

            dao.upsert(list)
        }
    }

    fun processStreamBatch(batch: List<StreamMessage<BaseRoom>>) {
        launch(dbContext) {
            val toRemove = HashSet<String>()
            val toInsert = ArrayList<ChatRoomEntity>(batch.size / 2)
            val toUpdate = ArrayList<ChatRoomEntity>(batch.size)
            batch.forEach {
                when(it.type) {
                    is Type.Removed -> toRemove.add(removeChatRoom(it.data))
                    is Type.Inserted -> insertChatRoom(it.data)?.let { toInsert.add(it) }
                    is Type.Updated -> {
                        when(it.data) {
                            is Subscription -> updateSubs[(it.data as Subscription).roomId] = it.data as Subscription
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

                Timber.d("Running ChatRooms transaction: remove: $toRemove - insert: $toInsert - update: $filteredUpdate")

                chatRoomDao().update(toRemove.toList(), filteredInsert, filteredUpdate)
            } catch (ex: Exception) {
                Timber.d(ex, "Error updating chatrooms")
            }
        }
    }

    fun updateSelfUser(myself: Myself) {
        launch(dbContext) {
            val user = userDao().getUser(myself.id)
            val entity = user?.copy(
                    name = myself.name ?: user.name,
                    username = myself.username ?: user.username,
                    utcOffset = myself.utcOffset ?: user.utcOffset,
                    status = myself.status?.toString() ?: user.status
            ) ?: myself.asUser().toEntity()

            Timber.d("UPDATING SELF: $entity")
            entity?.let { userDao().upsert(entity) }
        }
    }

    fun processRooms(rooms: List<ChatRoom>) {
        launch(dbContext) {
            val entities = rooms.map { mapChatRoom(it) }
            chatRoomDao().insertOrReplace(entities)
        }
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
        return when(data) {
            is Subscription -> data.roomId
            else -> data.id
        }
    }

    private suspend fun updateRoom(data: Room): ChatRoomEntity? {
        return chatRoomDao().get(data.id)?.let { current ->
            with(data) {
                val chatRoom = current.chatRoom

                lastMessage?.sender?.let { user ->
                    user.id?.let { id ->
                        if (findUser(id) == null) {
                            Timber.d("Missing last message user, inserting: $id")
                            insert(UserEntity(id, user.username, user.name))
                        }
                    }
                }

                user?.id?.let { id ->
                    if (findUser(id) == null) {
                        Timber.d("Missing owner user, inserting: $id")
                        insert(UserEntity(id, user!!.username, user!!.name))
                    }
                }

                chatRoom.copy(
                        name = name ?: chatRoom.name,
                        fullname = fullName ?: chatRoom.fullname,
                        ownerId = user?.id ?: chatRoom.ownerId,
                        readonly = readonly,
                        updatedAt = updatedAt ?: chatRoom.updatedAt,
                        lastMessageText = mapLastMessageText(lastMessage),
                        lastMessageUserId = lastMessage?.sender?.id,
                        lastMessageTimestamp = lastMessage?.timestamp
                )
            }
        }
    }

    private fun mapLastMessageText(message: Message?): String? {
        return if (message == null) {
            null
        } else {
            return if (message.message.isEmpty() && message.attachments?.isNotEmpty() == true) {
                mapAttachmentText(message.attachments!![0])
            } else {
                message.message
            }
        }
    }

    private fun mapAttachmentText(attachment: Attachment): String =
        context.getString(R.string.msg_sent_attachment)

    private suspend fun updateSubscription(data: Subscription): ChatRoomEntity? {
        return chatRoomDao().get(data.roomId)?.let { current ->
            with(data) {

                val userId = if (type is RoomType.DirectMessage) {
                    roomId.userId(user?.id)
                } else {
                    null
                }

                if (userId != null && findUser(userId) == null) {
                    Timber.d("Missing user, inserting: $userId")
                    insert(UserEntity(userId))
                }

                val chatRoom = current.chatRoom

                chatRoom.copy(
                        id = roomId,
                        subscriptionId = id,
                        type = type.toString(),
                        name = name ?: throw NullPointerException(), // this should be filtered on the SDK
                        fullname = fullName ?: chatRoom.fullname,
                        userId = userId ?: chatRoom.userId,
                        readonly = readonly ?: chatRoom.readonly,
                        isDefault = isDefault,
                        favorite = isFavorite,
                        open = open,
                        alert = alert,
                        unread = unread,
                        userMentions = userMentions ?: chatRoom.userMentions,
                        groupMentions = groupMentions ?: chatRoom.groupMentions,
                        updatedAt = updatedAt ?: chatRoom.updatedAt,
                        timestamp = timestamp ?: chatRoom.timestamp,
                        lastSeen = lastSeen ?: chatRoom.lastSeen
                )
            }
        }
    }

    private suspend fun insertChatRoom(data: BaseRoom): ChatRoomEntity? {
        return when(data) {
            is Room -> insertRoom(data)
            is Subscription -> insertSubscription(data)
            else -> null
        }
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

        if (userId != null && findUser(userId) == null) {
            Timber.d("Missing user, inserting: $userId")
            insert(UserEntity(userId))
        }

        room.lastMessage?.sender?.let { user ->
            user.id?.let { id ->
                if (findUser(id) == null) {
                    Timber.d("Missing last message user, inserting: $id")
                    insert(UserEntity(id, user.username, user.name))
                }
            }
        }

        room.user?.let { user ->
            user.id?.let { id ->
                if (findUser(id) == null) {
                    Timber.d("Missing owner user, inserting: $id")
                    insert(UserEntity(id, user.username, user.name))
                }
            }
        }

        return ChatRoomEntity(
            id = room.id,
            subscriptionId = subscription.id,
            type = room.type.toString(),
            name = room.name ?: subscription.name ?: throw NullPointerException(), // this should be filtered on the SDK
            fullname = subscription.fullName ?: room.fullName,
            userId = userId,
            ownerId = room.user?.id,
            readonly = subscription.readonly,
            isDefault = subscription.isDefault,
            favorite = subscription.isFavorite,
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

            lastMessage?.sender?.let { user ->
                user.id?.let { id ->
                    if (findUser(id) == null) {
                        Timber.d("Missing last message user, inserting: $id")
                        insert(UserEntity(id, user.username, user.name))
                    }
                }
            }

            user?.id?.let { id ->
                if (findUser(id) == null) {
                    Timber.d("Missing owner user, inserting: $id")
                    insert(UserEntity(id, user?.username, user?.name))
                }
            }

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
                broadcast = broadcast
            )
        }
    }

    suspend fun insert(rooms: List<ChatRoomEntity>) {
        withContext(dbContext) {
            chatRoomDao().cleanInsert(rooms)
        }
    }

    suspend fun insert(user: UserEntity) {
        withContext(dbContext) {
            userDao().insert(user)
        }
    }

    fun findUser(userId: String): String? = userDao().findUser(userId)
}

fun User.toEntity(): BaseUserEntity? {
    return if (name == null && username == null && utcOffset == null && status != null) {
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
            .replace("/","-")
            .replace(".", "_")

    return "$tmp.db"
}