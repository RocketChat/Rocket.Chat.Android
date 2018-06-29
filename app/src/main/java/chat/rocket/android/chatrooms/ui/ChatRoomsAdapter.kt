package chat.rocket.android.chatrooms.ui

import DateTimeHelper
import DrawableHelper
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.checkIfMyself
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.showLastMessage
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
import chat.rocket.android.util.extensions.*
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.unread_messages_badge.view.*

class ChatRoomsAdapter(
    private val context: Context,
    private val settings: PublicSettings,
    private val localRepository: LocalRepository,
    private val listener: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {

    var dataSet: MutableList<ChatRoom> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_chat))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    fun updateRooms(newRooms: List<ChatRoom>) {
        dataSet.clear()
        dataSet.addAll(newRooms)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, image_avatar)
            bindName(chatRoom, text_chat_name)
            bindIcon(chatRoom, image_chat_icon)
            if (settings.showLastMessage()) {
                text_last_message.setVisible(true)
                text_last_message_date_time.setVisible(true)
                bindLastMessageDateTime(chatRoom, text_last_message_date_time)
                bindLastMessage(chatRoom, text_last_message)
            } else {
                text_last_message.setVisible(false)
                text_last_message_date_time.setVisible(false)
            }
            bindUnreadMessages(chatRoom, text_total_unread_messages)

            if (chatRoom.alert || chatRoom.unread > 0) {
                text_chat_name.setTextColor(ContextCompat.getColor(context,
                    R.color.colorPrimaryText))
                text_last_message_date_time.setTextColor(ContextCompat.getColor(context,
                    R.color.colorAccent))
                text_last_message.setTextColor(ContextCompat.getColor(context,
                    android.R.color.primary_text_light))
            } else {
                text_chat_name.setTextColor(ContextCompat.getColor(context,
                    R.color.colorSecondaryText))
                text_last_message_date_time.setTextColor(ContextCompat.getColor(context,
                    R.color.colorSecondaryText))
                text_last_message.setTextColor(ContextCompat.getColor(context,
                    R.color.colorSecondaryText))
            }

            setOnClickListener { listener(chatRoom) }
        }

        private fun bindAvatar(chatRoom: ChatRoom, drawee: SimpleDraweeView) {
            if (chatRoom.type is RoomType.DirectMessage) {
                drawee.setImageURI(chatRoom.client.url.avatarUrl(chatRoom.name))
            } else {
                drawee.setImageURI(chatRoom.client.url.avatarUrl(chatRoom.name, true))
            }
        }

        private fun bindIcon(chatRoom: ChatRoom, imageView: ImageView) {
            val drawable = when (chatRoom.type) {
                is RoomType.Channel -> DrawableHelper.getDrawableFromId(
                    R.drawable.ic_hashtag_black_12dp,
                    context
                )
                is RoomType.PrivateGroup -> DrawableHelper.getDrawableFromId(
                    R.drawable.ic_lock_black_12_dp,
                    context
                )
                is RoomType.DirectMessage -> DrawableHelper.getUserStatusDrawable(
                    chatRoom.status,
                    context
                )
                else -> null
            }
            drawable?.let {
                val mutateDrawable = DrawableHelper.wrapDrawable(it).mutate()
                if (chatRoom.type !is RoomType.DirectMessage) {
                    val color = when (chatRoom.alert || chatRoom.unread > 0) {
                        true -> R.color.colorPrimaryText
                        false -> R.color.colorSecondaryText
                    }
                    DrawableHelper.tintDrawable(mutateDrawable, context, color)
                }
                imageView.setImageDrawable(mutateDrawable)
            }
        }

        private fun bindName(chatRoom: ChatRoom, textView: TextView) {
            textView.textContent = chatRoomName(chatRoom)
        }

        private fun chatRoomName(chatRoom: ChatRoom): String {
            return if (settings.useSpecialCharsOnRoom() || settings.useRealName()) {
                chatRoom.fullName ?: chatRoom.name
            } else {
                chatRoom.name
            }
        }

        private fun bindLastMessageDateTime(chatRoom: ChatRoom, textView: TextView) {
            val lastMessage = chatRoom.lastMessage
            if (lastMessage != null) {
                val localDateTime = DateTimeHelper.getLocalDateTime(lastMessage.timestamp)
                textView.content = DateTimeHelper.getDate(localDateTime, context)
            } else {
                textView.content = ""
            }
        }

        private fun bindLastMessage(chatRoom: ChatRoom, textView: TextView) {
            val lastMessage = chatRoom.lastMessage
            val lastMessageSender = lastMessage?.sender
            if (lastMessage != null && lastMessageSender != null) {
                val message = lastMessage.message
                val senderUsername = if (settings.useRealName()) {
                    lastMessageSender.name ?: lastMessageSender.username
                } else {
                    lastMessageSender.username
                }
                when (senderUsername) {
                    chatRoom.name -> {
                        textView.content = message
                    }
                    else -> {
                        val user = if (localRepository.checkIfMyself(lastMessageSender.username!!)) {
                            "${context.getString(R.string.msg_you)}: "
                        } else {
                            "$senderUsername: "
                        }
                        val spannable = SpannableStringBuilder(user)
                        val len = spannable.length
                        spannable.setSpan(ForegroundColorSpan(Color.BLACK), 0, len - 1, 0)
                        spannable.append(message)
                        textView.content = spannable
                    }
                }
            } else {
                textView.content = context.getText(R.string.msg_no_messages_yet)
            }
        }

        private fun bindUnreadMessages(chatRoom: ChatRoom, textView: TextView) {
            val totalUnreadMessage = chatRoom.unread
            when {
                totalUnreadMessage in 1..99 -> {
                    textView.textContent = totalUnreadMessage.toString()
                    textView.setVisible(true)
                }
                totalUnreadMessage > 99 -> {
                    textView.textContent = context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                    textView.setVisible(true)
                }
                else -> textView.setVisible(false)
            }
        }
    }
}