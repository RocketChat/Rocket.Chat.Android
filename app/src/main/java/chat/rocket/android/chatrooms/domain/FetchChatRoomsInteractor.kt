package chat.rocket.android.chatrooms.domain

import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.util.retryIO
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.userId
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class FetchChatRoomsInteractor(
    private val client: RocketChatClient,
    private val dbManager: DatabaseManager
) {

    suspend fun refreshChatRooms() {
        launch(CommonPool) {
            try {
                val rooms = retryIO("fetch chatRooms", times = 10,
                        initialDelay = 200, maxDelay = 2000) {
                    client.chatRooms().update.map { room ->
                        mapChatRoom(room)
                    }
                }

                Timber.d("Refreshing rooms: $rooms")
                dbManager.insert(rooms)
            } catch (ex: Exception) {
                Timber.d(ex, "Error getting chatrooms")
            }
        }
    }

    private suspend fun mapChatRoom(room: ChatRoom): ChatRoomEntity {
        with(room) {
            val userId = userId()
            if (userId != null && dbManager.findUser(userId) == null) {
                Timber.d("Missing user, inserting: $userId")
                dbManager.insert(UserEntity(userId))
            }
            lastMessage?.sender?.let { user ->
                user.id?.let { id ->
                    if (dbManager.findUser(id) == null) {
                        Timber.d("Missing last message user, inserting: $id")
                        dbManager.insert(UserEntity(id, user.username, user.name))
                    }
                }
            }
            user?.id?.let { id ->
                if (dbManager.findUser(id) == null) {
                    Timber.d("Missing owner user, inserting: $id")
                    dbManager.insert(UserEntity(id, user?.username, user?.name))
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
                    lastMessageText = lastMessage?.message,
                    lastMessageUserId = lastMessage?.sender?.id,
                    lastMessageTimestamp = lastMessage?.timestamp
            )
        }
    }
}