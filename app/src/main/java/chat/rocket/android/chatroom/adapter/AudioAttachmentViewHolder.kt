package chat.rocket.android.chatroom.adapter

import android.view.View
import androidx.core.view.isVisible
import chat.rocket.android.chatroom.uimodel.AudioAttachmentUiModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*

class AudioAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<AudioAttachmentUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
            image_attachment.isVisible = false
            audio_video_attachment.isVisible = true
        }
    }

    override fun bindViews(data: AudioAttachmentUiModel) {
        with(itemView) {
            file_name.text = data.attachmentTitle
            audio_video_attachment.setOnClickListener { view ->
                data.attachmentUrl.let { url ->
                    PlayerActivity.play(view.context, url)
                }
            }
        }
    }
}