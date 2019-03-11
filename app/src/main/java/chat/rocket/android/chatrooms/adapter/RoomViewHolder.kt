package chat.rocket.android.chatrooms.adapter

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.UserStatus
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.unread_messages_badge.view.*

class RoomViewHolder(itemView: View, private val listener: (RoomUiModel) -> Unit) :
    ViewHolder<RoomItemHolder>(itemView) {
    private val resources: Resources = itemView.resources
    private val channelIcon: Drawable = resources.getDrawable(R.drawable.ic_hashtag_12dp)
    private val groupIcon: Drawable = resources.getDrawable(R.drawable.ic_lock_12_dp)
    private val onlineIcon: Drawable = resources.getDrawable(R.drawable.ic_status_online_12dp)
    private val awayIcon: Drawable = resources.getDrawable(R.drawable.ic_status_away_12dp)
    private val busyIcon: Drawable = resources.getDrawable(R.drawable.ic_status_busy_12dp)
    private val offlineIcon: Drawable = resources.getDrawable(R.drawable.ic_status_invisible_12dp)

    override fun bindViews(data: RoomItemHolder) {
        val room = data.data
        with(itemView) {
            image_avatar.setImageURI(room.avatar)
            text_chat_name.text = room.name

            if (room.status != null && room.type is RoomType.DirectMessage) {
                image_chat_icon.setImageDrawable(getStatusDrawable(room.status))
            } else {
                image_chat_icon.setImageDrawable(getRoomDrawable(room.type))
            }

            if (room.lastMessage != null) {
                text_last_message.text = room.lastMessage
                text_last_message.isVisible = true
            } else {
                text_last_message.isGone = true
            }

            if (room.date != null) {
                text_timestamp.text = room.date
                text_timestamp.isVisible = true
            } else {
                text_timestamp.isInvisible = true
            }

            if (room.alert) {
                if (room.unread == null) text_total_unread_messages.text = "!"
                if (room.unread != null) text_total_unread_messages.text = room.unread
                if (room.mentions) text_total_unread_messages.text = "@${room.unread}"
                text_chat_name.setTextAppearance(context, R.style.ChatList_ChatName_Unread_TextView)
                text_timestamp.setTextAppearance(context, R.style.ChatList_Timestamp_Unread_TextView)
                text_last_message.setTextAppearance(context, R.style.ChatList_LastMessage_Unread_TextView)
                text_total_unread_messages.isVisible = true
            } else {
                text_chat_name.setTextAppearance(context, R.style.ChatList_ChatName_TextView)
                text_timestamp.setTextAppearance(context, R.style.ChatList_Timestamp_TextView)
                text_last_message.setTextAppearance(context, R.style.ChatList_LastMessage_TextView)
                text_total_unread_messages.isInvisible = true
            }

            setOnClickListener { listener(room) }
        }
    }

    private fun getRoomDrawable(type: RoomType): Drawable? {
        return when (type) {
            is RoomType.Channel -> channelIcon
            is RoomType.PrivateGroup -> groupIcon
            else -> null
        }
    }

    private fun getStatusDrawable(status: UserStatus): Drawable {
        return when (status) {
            is UserStatus.Online -> onlineIcon
            is UserStatus.Away -> awayIcon
            is UserStatus.Busy -> busyIcon
            else -> offlineIcon
        }
    }
}