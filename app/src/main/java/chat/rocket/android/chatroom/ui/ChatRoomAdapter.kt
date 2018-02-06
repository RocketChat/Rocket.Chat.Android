package chat.rocket.android.chatroom.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.AttachmentType
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import kotlinx.android.synthetic.main.message_attachment.view.*

class ChatRoomAdapter: RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {
    private val dataSet = ArrayList<MessageViewModel>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_message))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(dataSet[position], dataSet.getOrNull(position + 1))

    override fun getItemCount(): Int =
        dataSet.size

    override fun getItemViewType(position: Int): Int =
        position

    override fun getItemId(position: Int): Long =
        dataSet[position].id.hashCode().toLong()

    fun addDataSet(dataSet: List<MessageViewModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(previousDataSetSize, dataSet)
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    fun addItem(message: MessageViewModel) {
        dataSet.add(0, message)
        notifyItemInserted(0)
    }

    fun updateItem(index: Int, message: MessageViewModel) {
        dataSet[index] = message
        notifyItemChanged(index)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: MessageViewModel, nextMessage: MessageViewModel?) = with(itemView) {
            image_avatar.setImageURI(message.avatarUri)
            text_sender.text = message.senderName
            text_message_time.text = message.time

            if (nextMessage != null) {
                if (isSequential(message, nextMessage)) {
                    image_avatar.setVisible(false)
                    text_sender.setVisible(false)
                    text_message_time.setVisible(false)
                }
            }

            text_content.text = message.content
            bindAttachment(message, message_attachment, image_attachment, audio_video_attachment, file_name)
        }

        private fun isSequential(message: MessageViewModel, nextMessage: MessageViewModel): Boolean =
            (message.isGroupable && nextMessage.isGroupable) && (message.senderId == nextMessage.senderId)

        private fun bindAttachment(message: MessageViewModel,
                                   attachment_container: View,
                                   image_attachment: SimpleDraweeView,
                                   audio_video_attachment: View,
                                   file_name: TextView) {
            with(message) {
                if (attachmentUrl == null || attachmentType == null) {
                    attachment_container.setVisible(false)
                    return
                }

                var imageVisible = false
                var videoVisible = false

                attachment_container.setVisible(true)
                when (message.attachmentType) {
                    is AttachmentType.Image -> {
                        imageVisible = true
                        image_attachment.setImageURI(message.attachmentUrl)
                        image_attachment.setOnClickListener { view ->
                            // TODO - implement a proper image viewer with a proper Transition
                            ImageViewer.Builder(view.context, listOf(message.attachmentUrl))
                                    .setStartPosition(0)
                                    .show()
                        }
                    }
                    is AttachmentType.Video,
                    is AttachmentType.Audio -> {
                        videoVisible = true
                        audio_video_attachment.setOnClickListener { view ->
                            message.attachmentUrl?.let { url ->
                                PlayerActivity.play(view.context, url)
                            }
                        }
                    }
                }

                image_attachment.setVisible(imageVisible)
                audio_video_attachment.setVisible(videoVisible)
                file_name.text = message.attachmentTitle
            }
        }
    }
}