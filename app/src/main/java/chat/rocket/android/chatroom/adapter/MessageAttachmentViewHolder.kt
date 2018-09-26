package chat.rocket.android.chatroom.adapter

import android.animation.ValueAnimator
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.MessageAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_message_attachment.view.*

class MessageAttachmentViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<MessageAttachmentUiModel>(itemView, listener, reactionListener) {

    private var expanded = true

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: MessageAttachmentUiModel) {
        with(itemView) {
            val collapsedHeight = context.resources.getDimensionPixelSize(R.dimen.quote_collapsed_height)
            val viewMore = context.getString(R.string.msg_view_more)
            val viewLess = context.getString(R.string.msg_view_less)
            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
            text_view_more.text = viewLess
            text_content.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

                override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int,
                                            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    val textMeasuredHeight = bottom - top
                    if (collapsedHeight >= textMeasuredHeight) {
                        text_view_more.isVisible = false
                        text_content.removeOnLayoutChangeListener(this)
                        return
                    }

                    text_view_more.isVisible = true

                    val expandAnimation = ValueAnimator
                        .ofInt(collapsedHeight, textMeasuredHeight)
                        .setDuration(300)
                    expandAnimation.interpolator = LinearInterpolator()

                    val collapseAnimation = ValueAnimator
                        .ofInt(textMeasuredHeight, collapsedHeight)
                        .setDuration(300)
                    collapseAnimation.interpolator = LinearInterpolator()

                    val lp = text_content.layoutParams

                    expandAnimation.addUpdateListener {
                        val value = it.animatedValue as Int
                        lp.height = value
                        text_content.layoutParams = lp
                        expanded = if (value == textMeasuredHeight) {
                            text_view_more.text = viewLess
                            true
                        } else {
                            text_view_more.text = viewMore
                            false
                        }
                    }

                    collapseAnimation.addUpdateListener {
                        val value = it.animatedValue as Int
                        lp.height = value
                        text_content.layoutParams = lp
                        expanded = if (value == textMeasuredHeight) {
                            text_view_more.text = viewLess
                            true
                        } else {
                            text_view_more.text = viewMore
                            false
                        }
                    }

                    text_view_more.setOnClickListener {
                        if (expandAnimation.isRunning) return@setOnClickListener

                        if (expanded) {
                            collapseAnimation.start()
                        } else {
                            expandAnimation.start()
                        }
                    }

                    text_content.removeOnLayoutChangeListener(this)
                }
            })
        }
    }
}