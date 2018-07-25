package chat.rocket.android.chatrooms.adapter

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.db.model.ChatRoom
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.checkIfMyself
import chat.rocket.android.server.domain.GetCurrentUserInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.showLastMessage
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.date
import chat.rocket.android.util.extensions.localDateTime
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.User
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.model.userStatusOf
import chat.rocket.core.model.Room
import chat.rocket.core.model.SpotlightResult

class RoomUiModelMapper(
    private val context: Application,
    private val settings: PublicSettings,
    private val userInteractor: GetCurrentUserInteractor,
    private val serverUrl: String
) {
    private val nameUnreadColor = ContextCompat.getColor(context, R.color.colorPrimaryText)
    private val nameColor = ContextCompat.getColor(context, R.color.colorSecondaryText)
    private val dateUnreadColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val dateColor = ContextCompat.getColor(context, R.color.colorSecondaryText)
    private val messageUnreadColor = ContextCompat.getColor(context, android.R.color.primary_text_light)
    private val messageColor = ContextCompat.getColor(context, R.color.colorSecondaryText)

    private val currentUser by lazy {
        userInteractor.get()
    }

    fun map(rooms: List<ChatRoom>, grouped: Boolean = false): List<ItemHolder<*>> {
        val list = ArrayList<ItemHolder<*>>(rooms.size + 4)
        var lastType: String? = null
        rooms.forEach { room ->
            if (grouped && lastType != room.chatRoom.type) {
                list.add(HeaderItemHolder(roomType(room.chatRoom.type)))
            }
            list.add(RoomItemHolder(map(room)))
            lastType = room.chatRoom.type
        }

        return list
    }

    fun map(spotlight: SpotlightResult): List<ItemHolder<*>> {
        val list = ArrayList<ItemHolder<*>>(spotlight.users.size + spotlight.rooms.size)
        spotlight.users.filterNot { it.username.isNullOrEmpty() }.forEach { user ->
            list.add(RoomItemHolder(mapUser(user)))
        }
        spotlight.rooms.filterNot { it.name.isNullOrEmpty() }.forEach { room ->
            list.add(RoomItemHolder(mapRoom(room)))
        }

        return list
    }

    private fun mapUser(user: User): RoomUiModel {
        return with(user) {
            val name = mapName(user.username!!, user.name, false)
            val status = user.status
            val avatar = serverUrl.avatarUrl(user.username!!)
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
    }

    private fun mapRoom(room: Room): RoomUiModel {
        return with(room) {
            RoomUiModel(
                id = id,
                name = name!!,
                type = type,
                avatar = serverUrl.avatarUrl(name!!, isGroupOrChannel = true),
                lastMessage = mapLastMessage(lastMessage?.sender?.id, lastMessage?.sender?.username,
                        lastMessage?.sender?.name, lastMessage?.message,
                        isDirectMessage = type is RoomType.DirectMessage)
            )
        }
    }

    fun map(chatRoom: ChatRoom): RoomUiModel {
        return with(chatRoom.chatRoom) {
            val isUnread = alert || unread > 0
            val type = roomTypeOf(type)
            val status = chatRoom.status?.let { userStatusOf(it) }
            val roomName = mapName(name, fullname, isUnread)
            val timestamp = mapDate(lastMessageTimestamp ?: updatedAt, isUnread)
            val avatar = if (type is RoomType.DirectMessage) {
                serverUrl.avatarUrl(name)
            } else {
                serverUrl.avatarUrl(name, isGroupOrChannel = true)
            }
            val unread = mapUnread(unread)
            val lastMessage = mapLastMessage(lastMessageUserId, chatRoom.lastMessageUserName,
                    chatRoom.lastMessageUserFullName, lastMessageText, isUnread,
                    type is RoomType.DirectMessage)
            val open = open

            RoomUiModel(
                id = id,
                name = roomName,
                type = type,
                avatar = avatar,
                open = open,
                date = timestamp,
                unread = unread,
                alert = isUnread,
                lastMessage = lastMessage,
                status = status,
                username = if (type is RoomType.DirectMessage) name else null
            )
        }
    }

    private fun roomType(type: String): String {
        val resources = context.resources
        return when (type) {
            RoomType.CHANNEL -> resources.getString(R.string.header_channel)
            RoomType.PRIVATE_GROUP -> resources.getString(R.string.header_private_groups)
            RoomType.DIRECT_MESSAGE -> resources.getString(R.string.header_direct_messages)
            RoomType.LIVECHAT -> resources.getString(R.string.header_live_chats)
            else -> resources.getString(R.string.header_unknown)
        }
    }

    private fun mapLastMessage(userId: String?, name: String?, fullName: String?, text: String?,
                               unread: Boolean = false,
                               isDirectMessage: Boolean = false): CharSequence? {
        return if (!settings.showLastMessage()) {
            null
        } else if (name != null && text != null) {
            val user = if (currentUser != null && currentUser!!.id == userId) {
                "${context.getString(R.string.msg_you)}: "
            } else {
                if (isDirectMessage) "" else "${mapName(name, fullName, unread)}: "
            }

            val color = if (unread) messageUnreadColor else messageColor

            SpannableStringBuilder()
                    .color(color) {
                        bold { append(user) }
                        append(text)
                    }
        } else {
            context.getText(R.string.msg_no_messages_yet)
        }
    }

    private fun mapName(name: String, fullName: String?, unread: Boolean): CharSequence {
        val roomName = if (settings.useSpecialCharsOnRoom() || settings.useRealName()) {
            fullName ?: name
        } else {
            name
        }

        val color = if (unread) nameUnreadColor else nameColor
        return SpannableStringBuilder()
                .color(color) {
                    append(roomName)
                }
    }

    private fun mapUnread(unread: Long): String? {
        return when(unread) {
            0L -> null
            in 1..99 -> unread.toString()
            else -> context.getString(R.string.msg_more_than_ninety_nine_unread_messages)

        }
    }

    private fun mapDate(date: Long?, unread: Boolean): CharSequence? {
        return date?.localDateTime()?.date(context)?.let {
            val color = if (unread) dateUnreadColor else dateColor
            SpannableStringBuilder().color(color) {
                append(it)
            }
        }
    }
}