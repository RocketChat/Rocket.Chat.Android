package chat.rocket.android.chatroom.ui

import DrawableHelper
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.AttachmentType
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setImageURI
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.TextAvatarDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.item_message.view.*
import kotlinx.android.synthetic.main.message_attachment.view.*

class PinnedMessagesAdapter : RecyclerView.Adapter<PinnedMessagesAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val dataSet = ArrayList<MessageViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_message))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    fun addDataSet(dataSet: List<MessageViewModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(previousDataSetSize, dataSet)
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    fun addItem(message: MessageViewModel) {
        dataSet.add(0, message)
        notifyItemInserted(0)
    }

    fun updateItem(message: MessageViewModel) {
        val index = dataSet.indexOfFirst { it.id == message.id }
        if (index > -1) {
            dataSet[index] = message
            notifyItemChanged(index)
        }
    }

    fun removeItem(messageId: String) {
        val index = dataSet.indexOfFirst { it.id == messageId }
        if (index > -1) {
            dataSet.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.hashCode().toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var messageViewModel: MessageViewModel

        fun bind(message: MessageViewModel) = with(itemView) {
            messageViewModel = message
            val placeholder = TextAvatarDrawable(message.currentUsername ?: "",
                    DrawableHelper.getAvatarBackgroundColor(message.currentUsername ?: "default"))
            image_avatar.setImageURI(message.avatarUri) {
                placeholder(placeholder)
                error(placeholder)
            }
            text_sender.content = message.senderName
            text_message_time.content = message.time
            text_content.content = message.content
            text_content.movementMethod = LinkMovementMethod()

            bindAttachment(message, message_attachment, image_attachment, audio_video_attachment,
                    file_name)
        }

        private fun bindAttachment(message: MessageViewModel,
                                   attachment_container: View,
                                   image_attachment: ImageView,
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
                        image_attachment.setImageURI(message.attachmentUrl) {
                            placeholder(R.drawable.image_dummy)
                            centerCrop()
                            transition(DrawableTransitionOptions.withCrossFade())
                        }
                        image_attachment.setOnClickListener { view ->
                            // TODO - implement a proper image viewer with a proper Transition
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