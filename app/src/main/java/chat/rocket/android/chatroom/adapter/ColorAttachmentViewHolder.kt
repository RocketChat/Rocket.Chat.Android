package chat.rocket.android.chatroom.adapter

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.ColorAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_color_attachment.view.*


class ColorAttachmentViewHolder(itemView: View,
                                listener: BaseViewHolder.ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<ColorAttachmentUiModel>(itemView, listener, reactionListener) {

    val drawable: Drawable = ColorDrawable(ContextCompat.getColor(itemView.context, R.color.quoteBar))

    init {
        with(itemView) {
            setupActionMenu(color_attachment_container)
            attachment_text.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: ColorAttachmentUiModel) {
        with(itemView) {
                quote_bar.setColorFilter(data.color)
                if (data.text.isNotEmpty()) {
                    attachment_text.isVisible = true
                    attachment_text.text = data.text
                } else {
                    attachment_text.isVisible = false
                }

                if (data.fields.isNullOrEmpty()) {
                    text_fields.isVisible = false
                } else {
                    text_fields.isVisible = true
                    text_fields.text = data.fields
                }
        }
    }

}