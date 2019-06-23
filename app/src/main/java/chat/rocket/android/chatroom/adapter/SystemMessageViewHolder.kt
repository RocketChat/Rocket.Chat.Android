package chat.rocket.android.chatroom.adapter

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import chat.rocket.android.chatroom.uimodel.SystemMessageUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.core.model.MessageType
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_system_message.view.*

class SystemMessageViewHolder(
    itemView: View,
    reactionListener: EmojiReactionListener? = null,
    private val avatarListener: (String) -> Unit,
    private val joinVideoCallListener: (View) -> Unit
) : BaseViewHolder<SystemMessageUiModel>(itemView, null, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(message_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: SystemMessageUiModel) {
        with(itemView) {
            day.text = data.currentDayMarkerText
            day_marker_layout.isVisible = data.showDayMarker

            new_messages_notif.isVisible = data.isFirstUnread

            text_sender.text = data.senderName
            text_content.text_content.text = data.content

            button_join_video_call.isVisible = data.message.type is MessageType.JitsiCallStarted
            button_join_video_call.setOnClickListener { joinVideoCallListener(it) }

            image_avatar.setImageURI(data.avatar)
            text_content.setTextColor(if (data.isTemporary) Color.GRAY else Color.BLACK)

            setOnClickListener {
                data.message.sender?.id?.let { userId ->
                    avatarListener(userId)
                }
            }
        }
    }
}
