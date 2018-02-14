package chat.rocket.android.chatroom.ui

import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.ui.bottomsheet.BottomSheetMenu
import chat.rocket.android.chatroom.ui.bottomsheet.adapter.ActionListAdapter
import chat.rocket.android.chatroom.viewmodel.AttachmentType
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.content
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import kotlinx.android.synthetic.main.message_attachment.view.*
import ru.whalemare.sheetmenu.extension.inflate
import ru.whalemare.sheetmenu.extension.toList

class ChatRoomAdapter(private val roomType: String,
                      private val roomName: String,
                      private val presenter: ChatRoomPresenter) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {
    private val dataSet = ArrayList<MessageViewModel>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_message), roomType, roomName, presenter)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = dataSet[position].id.hashCode().toLong()

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

    class ViewHolder(itemView: View,
                     val roomType: String,
                     val roomName: String,
                     val presenter: ChatRoomPresenter) : RecyclerView.ViewHolder(itemView), MenuItem.OnMenuItemClickListener {
        private lateinit var messageViewModel: MessageViewModel

        fun bind(message: MessageViewModel) = with(itemView) {
            messageViewModel = message

            image_avatar.setImageURI(message.avatarUri)
            text_sender.text = message.senderName
            text_message_time.content = message.time
            text_content.content = message.content
            text_content.movementMethod = LinkMovementMethod()
            bindAttachment(message, message_attachment, image_attachment, audio_video_attachment, file_name)

            text_content.setOnClickListener {
                if (!message.isSystemMessage) {
                    val menuItems = it.context.inflate(R.menu.message_actions).toList()
                    menuItems.find { it.itemId == R.id.action_menu_msg_pin_unpin }?.apply {
                        val isPinned = message.isPinned
                        setTitle(if (isPinned) R.string.action_msg_unpin else R.string.action_msg_pin)
                        setChecked(isPinned)
                    }
                    val adapter = ActionListAdapter(menuItems, this@ViewHolder)
                    BottomSheetMenu(adapter).apply {

                    }.show(it.context)
                }
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            messageViewModel.apply {
                when (item.itemId) {
                    R.id.action_menu_msg_delete -> presenter.deleteMessage(roomId, id)
                    R.id.action_menu_msg_quote -> presenter.citeMessage(roomType, roomName, id, false)
                    R.id.action_menu_msg_reply -> presenter.citeMessage(roomType, roomName, id, true)
                    R.id.action_menu_msg_copy -> presenter.copyMessage(id)
                    R.id.action_menu_msg_edit -> presenter.editMessage(roomId, id, getOriginalMessage())
                    R.id.action_menu_msg_pin_unpin -> {
                        with(item) {
                            if (!isChecked) {
                                presenter.pinMessage(id)
                            } else {
                                presenter.unpinMessage(id)
                            }
                        }
                    }
                    else -> TODO("Not implemented")
                }
            }
            return true
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
    }
}