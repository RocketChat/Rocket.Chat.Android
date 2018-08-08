package chat.rocket.android.chatroom.adapter

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import chat.rocket.android.chatroom.uimodel.AuthorAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.util.extensions.content
import chat.rocket.common.util.ifNull
import kotlinx.android.synthetic.main.item_author_attachment.view.*

class AuthorAttachmentViewHolder(itemView: View,
                                 listener: ActionsListener,
                                 reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<AuthorAttachmentUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(author_attachment_container)
        }
    }

    override fun bindViews(data: AuthorAttachmentUiModel) {
        with(itemView) {
            data.icon?.let { icon ->
                author_icon.isVisible = true
                author_icon.setImageURI(icon)
            }.ifNull {
                author_icon.isGone = true
            }

            author_icon.setImageURI(data.icon)
            text_author_name.content = data.name

            data.fields?.let { fields ->
                text_fields.content = fields
                text_fields.isVisible = true
            }.ifNull {
                text_fields.isGone = true
            }

            text_author_name.setOnClickListener {
                it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.attachmentUrl)))
            }
        }
    }
}