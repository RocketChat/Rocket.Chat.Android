package chat.rocket.android.chatroom.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.view.View
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.isSystemMessage
import com.bumptech.glide.load.resource.gif.GifDrawable
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null,
    private val avatarListener: (String) -> Unit,
    private val joinVideoCallListener: (View) -> Unit
) : BaseViewHolder<MessageUiModel>(itemView, listener, reactionListener), Drawable.Callback {

    init {
        with(itemView) {
            setupActionMenu(message_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: MessageUiModel) {
        with(itemView) {
            day.text = data.currentDayMarkerText
            day_marker_layout.isVisible = data.showDayMarker

            new_messages_notif.isVisible = data.isFirstUnread

            text_message_time.text = data.time
            text_sender.text = data.senderName

            if (data.content is Spannable) {
                val spans = data.content.getSpans(0, data.content.length, ImageSpan::class.java)
                spans.forEach {
                    if (it.drawable is GifDrawable) {
                        it.drawable.callback = this@MessageViewHolder
                        (it.drawable as GifDrawable).start()
                    }
                }
            }

            text_content.text_content.text = data.content

            button_join_video_call.isVisible = data.message.type is MessageType.JitsiCallStarted
            button_join_video_call.setOnClickListener { joinVideoCallListener(it) }

            image_avatar.setImageURI(data.avatar)
            text_content.setTextColor(if (data.isTemporary) Color.GRAY else Color.BLACK)

            data.message.let {
                text_edit_indicator.isVisible = !it.isSystemMessage() && it.editedBy != null
                image_star_indicator.isVisible = it.starred?.isNotEmpty() ?: false
            }

            if (data.unread == null) {
                read_receipt_view.isVisible = false
            } else {
                read_receipt_view.setImageResource(
                    if (data.unread == true) {
                        R.drawable.ic_check_unread_24dp
                    } else {
                        R.drawable.ic_check_read_24dp
                    }
                )
                read_receipt_view.isVisible = true
            }

            image_avatar.setOnClickListener {
                data.message.sender?.id?.let { userId ->
                    avatarListener(userId)
                }
            }
        }
    }

    override fun unscheduleDrawable(who: Drawable?, what: Runnable?) {
        with(itemView) {
            text_content.removeCallbacks(what)
        }
    }

    override fun invalidateDrawable(p0: Drawable?) {
        with(itemView) {
            text_content.invalidate()
        }
    }

    override fun scheduleDrawable(who: Drawable?, what: Runnable?, w: Long) {
        with(itemView) {
            text_content.postDelayed(what, w)
        }
    }
}
