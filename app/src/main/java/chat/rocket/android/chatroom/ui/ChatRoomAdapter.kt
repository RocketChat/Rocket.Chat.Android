package chat.rocket.android.chatroom.ui

import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.viewmodel.AttachmentType
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.content
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisible
import chat.rocket.common.util.ifNull
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import kotlinx.android.synthetic.main.message_attachment.view.*

class ChatRoomAdapter(private val serverUrl: String,
                      private val presenter: ChatRoomPresenter) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val dataSet = ArrayList<MessageViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_message), serverUrl, presenter)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

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

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.hashCode().toLong()
    }

    class ViewHolder(itemView: View, val serverUrl: String, val presenter: ChatRoomPresenter) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: MessageViewModel) = with(itemView) {
            bindUserAvatar(message, image_avatar, image_unknown_avatar)
            text_user_name.content = message.sender
            text_message_time.content = message.time
            text_content.content = message.content
            text_content.movementMethod = LinkMovementMethod()

            bindAttachment(message, message_attachment, image_attachment, audio_video_attachment,
                    file_name)

            text_content.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.menuInflater.inflate(R.menu.message_actions, popup.menu)
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_menu_msg_delete -> presenter.deleteMessage(message.roomId, message.id)
                        else -> TODO("Not implemented")
                    }
                    true
                }
                popup.show()
            }
        }

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

        private fun bindUserAvatar(message: MessageViewModel, drawee: SimpleDraweeView, imageUnknownAvatar: ImageView) = message.getAvatarUrl(serverUrl).let {
            drawee.setImageURI(it.toString())
            drawee.setVisible(true)
            imageUnknownAvatar.setVisible(false)
        }.ifNull {
            drawee.setVisible(false)
            imageUnknownAvatar.setVisible(true)
        }
    }
}
