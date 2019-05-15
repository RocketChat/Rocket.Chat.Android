package chat.rocket.android.chatrooms.adapter

import android.app.Application
import android.text.SpannableStringBuilder
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.db.model.ChatRoom
import chat.rocket.android.server.domain.GetCurrentUserInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.showLastMessage
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.date
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.android.util.extensions.localDateTime
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.User
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.model.userStatusOf
import chat.rocket.core.model.Room
import chat.rocket.core.model.SpotlightResult
import ru.noties.markwon.Markwon

class RoomUiModelMapper(
    private val context: Application,
    private val settings: PublicSettings,
    private val userInteractor: GetCurrentUserInteractor,
    private val tokenRepository: TokenRepository,
    private val serverUrl: String,
    private val permissions: PermissionsInteractor
) {
    private val currentUser by lazy { userInteractor.get() }
    private val token by lazy { tokenRepository.get(serverUrl) }

    fun map(
        rooms: List<ChatRoom>,
        grouped: Boolean = false,
        showLastMessage: Boolean = true
    ): List<ItemHolder<*>> {
        val list = ArrayList<ItemHolder<*>>(rooms.size + 5)
        var lastType: String? = null
        if (grouped) {
            val favRooms = rooms.filter { it.chatRoom.favorite == true }
            val unfavRooms = rooms.filterNot { it.chatRoom.favorite == true }

            if (favRooms.isNotEmpty()) {
                list.add(HeaderItemHolder(context.resources.getString(R.string.header_favorite)))
            }
            favRooms.forEach { room ->
                list.add(RoomItemHolder(map(room, showLastMessage)))
            }
            unfavRooms.forEach { room ->
                if (lastType != room.chatRoom.type) {
                    list.add(HeaderItemHolder(roomType(room.chatRoom.type)))
                }
                list.add(RoomItemHolder(map(room, showLastMessage)))
                lastType = room.chatRoom.type
            }
        } else {
            rooms.forEach { room ->
                list.add(RoomItemHolder(map(room, showLastMessage)))
            }
        }

        return list
    }

    fun map(spotlight: SpotlightResult, showLastMessage: Boolean = true): List<ItemHolder<*>> {

        val list = ArrayList<ItemHolder<*>>(spotlight.users.size + spotlight.rooms.size)
        spotlight.users.filterNot { it.username.isNullOrEmpty() }.forEach { user ->
            list.add(RoomItemHolder(mapUser(user)))
        }
        spotlight.rooms.filterNot { it.name.isNullOrEmpty() }.forEach { room ->
            list.add(RoomItemHolder(mapRoom(room, showLastMessage)))
        }

        return list
    }

    private fun mapUser(user: User): RoomUiModel = with(user) {
        val name = mapName(user.username!!, user.name)
        val status = user.status
        val avatar = serverUrl.avatarUrl(user.username!!, token?.userId, token?.authToken)
        val username = user.username!!

        RoomUiModel(
                id = user.id,
                name = name,
                type = roomTypeOf(RoomType.DIRECT_MESSAGE),
                avatar = avatar,
                status = status,
                username = username
        )
    }

    private fun mapRoom(room: Room, showLastMessage: Boolean = true): RoomUiModel = with(room) {
        RoomUiModel(
                id = id,
                name = name!!,
                type = type,
                avatar = serverUrl.avatarUrl(name!!, token?.userId, token?.authToken, isGroupOrChannel = true),
                lastMessage = if (showLastMessage) {
                    mapLastMessage(
                        lastMessage?.sender?.id,
                        lastMessage?.sender?.username,
                        lastMessage?.sender?.name,
                        lastMessage?.message,
                        isDirectMessage = type is RoomType.DirectMessage
                    )
                } else {
                    null
                },
                muted = muted.orEmpty(),
                writable = isChannelWritable(muted)
        )
    }

    fun map(chatRoom: ChatRoom, showLastMessage: Boolean = true): RoomUiModel =
        with(chatRoom.chatRoom) {
            val isUnread = alert || unread > 0
            val type = roomTypeOf(type)
            val status = chatRoom.status?.let { userStatusOf(it) }
            val roomName = mapName(name, fullname)
            val favorite = favorite
            val timestamp = mapDate(lastMessageTimestamp ?: updatedAt)
            val avatar =
                if (type is RoomType.DirectMessage) {
                    serverUrl.avatarUrl(name, token?.userId, token?.authToken)
                } else {
                    serverUrl.avatarUrl(name, token?.userId, token?.authToken, isGroupOrChannel = true)
                }
            val unread = mapUnread(unread)
            val lastMessage = if (showLastMessage) {
                mapLastMessage(
                    lastMessageUserId,
                    chatRoom.lastMessageUserName,
                    chatRoom.lastMessageUserFullName,
                    lastMessageText,
                    type is RoomType.DirectMessage
                )
            } else {
                null
            }
            val hasMentions = mapMentions(userMentions, groupMentions)
            val open = open
            val lastMessageMarkdown =
                lastMessage?.let { Markwon.markdown(context, it.toString()).toString() }

            RoomUiModel(
                id = id,
                isDiscussion = parentId.isNotNullNorEmpty(),
                name = roomName,
                type = type,
                avatar = avatar,
                open = open,
                date = timestamp,
                unread = unread,
                mentions = hasMentions,
                favorite = favorite,
                alert = isUnread,
                lastMessage = lastMessageMarkdown,
                status = status,
                username = if (type is RoomType.DirectMessage) name else null,
                muted = muted.orEmpty(),
                writable = isChannelWritable(muted)
            )
        }

    private fun isChannelWritable(muted: List<String>?): Boolean {
        val canWriteToReadOnlyChannels = permissions.canPostToReadOnlyChannels()
        return canWriteToReadOnlyChannels || !muted.orEmpty().contains(currentUser?.username)
    }

    private fun roomType(type: String): String = with(context.resources) {
        when (type) {
            RoomType.CHANNEL -> getString(R.string.msg_channels)
            RoomType.PRIVATE_GROUP -> getString(R.string.header_private_groups)
            RoomType.DIRECT_MESSAGE -> getString(R.string.header_direct_messages)
            RoomType.LIVECHAT -> getString(R.string.header_live_chats)
            else -> getString(R.string.header_unknown)
        }
    }

    private fun mapLastMessage(
        userId: String?,
        name: String?,
        fullName: String?,
        text: String?,
        isDirectMessage: Boolean = false
    ): CharSequence? {
        return if (!settings.showLastMessage()) {
            null
        } else if (name != null && text != null) {
            val user = if (currentUser != null && currentUser!!.id == userId) {
                "${context.getString(R.string.msg_you)}: "
            } else {
                if (isDirectMessage) "" else "${mapName(name, fullName)}: "
            }
            SpannableStringBuilder().append(user).append(text)
        } else {
            context.getText(R.string.msg_no_messages_yet)
        }
    }

    private fun mapName(name: String, fullName: String?): CharSequence {
        return if (settings.useSpecialCharsOnRoom() || settings.useRealName()) {
            fullName ?: name
        } else {
            name
        }
    }

    private fun mapUnread(unread: Long): String? = when (unread) {
        0L -> null
        in 1..99 -> unread.toString()
        else -> context.getString(R.string.msg_more_than_ninety_nine_unread_messages)

    }

    private fun mapMentions(userMentions: Long?, groupMentions: Long?): Boolean {
        if (userMentions != null && groupMentions != null) {
            if (userMentions > 0 || groupMentions > 0) {
                return true
            }
        }
        return false
    }

    private fun mapDate(date: Long?): CharSequence? = date?.localDateTime()?.date(context)
}
