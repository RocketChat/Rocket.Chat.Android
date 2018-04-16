package chat.rocket.android.chatroom.adapter

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.ColorAttachmentViewModel
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_color_attachment.view.*


class ColorAttachmentViewHolder(itemView: View,
                                listener: BaseViewHolder.ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<ColorAttachmentViewModel>(itemView, listener, reactionListener) {

    val drawable: Drawable? = ContextCompat.getDrawable(itemView.context,
            R.drawable.quote_vertical_bar)

    init {
        with(itemView) {
            setupActionMenu(attachment_text)
            setupActionMenu(color_attachment_container)
            attachment_text.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: ColorAttachmentViewModel) {
        with(itemView) {
            drawable?.let {
                quote_bar.background = drawable.mutate().apply { setTint(data.color) }
                attachment_text.text = data.text
            }
        }
    }

}