package chat.rocket.android.chatroom.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.uimodel.*
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.widget.emoji.EmojiReactionListener
import chat.rocket.core.model.Message
import chat.rocket.core.model.isSystemMessage
import timber.log.Timber
import java.security.InvalidParameterException

class ChatRoomAdapter(
    private val roomType: String? = null,
    private val roomName: String? = null,
    private val presenter: ChatRoomPresenter? = null,
    private val enableActions: Boolean = true,
    private val reactionListener: EmojiReactionListener? = null
) : RecyclerView.Adapter<BaseViewHolder<*>>() {
    private val dataSet = ArrayList<BaseUiModel<*>>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType.toViewType()) {
            BaseUiModel.ViewType.MESSAGE -> {
                val view = parent.inflate(R.layout.item_message)
                MessageViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.IMAGE_ATTACHMENT -> {
                val view = parent.inflate(R.layout.message_attachment)
                ImageAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.AUDIO_ATTACHMENT -> {
                val view = parent.inflate(R.layout.message_attachment)
                AudioAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.VIDEO_ATTACHMENT -> {
                val view = parent.inflate(R.layout.message_attachment)
                VideoAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.URL_PREVIEW -> {
                val view = parent.inflate(R.layout.message_url_preview)
                UrlPreviewViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.MESSAGE_ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_message_attachment)
                MessageAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.AUTHOR_ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_author_attachment)
                AuthorAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.COLOR_ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_color_attachment)
                ColorAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.GENERIC_FILE_ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_file_attachment)
                GenericFileAttachmentViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.MESSAGE_REPLY -> {
                val view = parent.inflate(R.layout.item_message_reply)
                MessageReplyViewHolder(view, actionsListener, reactionListener) { roomName, permalink ->
                    presenter?.openDirectMessage(roomName, permalink)
                }
            }
            else -> {
                throw InvalidParameterException("TODO - implement for ${viewType.toViewType()}")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataSet[position].viewType
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder !is MessageViewHolder) {
            if (position + 1 < itemCount) {
                val messageAbove = dataSet[position + 1]
                if (messageAbove.messageId == dataSet[position].messageId) {
                    messageAbove.nextDownStreamMessage = dataSet[position]
                }
            }
        } else {
            if (position == 0) {
                dataSet[0].nextDownStreamMessage = null
            } else if (position - 1 > 0) {
                if (dataSet[position - 1].messageId != dataSet[position].messageId) {
                    dataSet[position].nextDownStreamMessage = null
                }
            }
        }

        when (holder) {
            is MessageViewHolder ->
                holder.bind(dataSet[position] as MessageUiModel)
            is ImageAttachmentViewHolder ->
                holder.bind(dataSet[position] as ImageAttachmentUiModel)
            is AudioAttachmentViewHolder ->
                holder.bind(dataSet[position] as AudioAttachmentUiModel)
            is VideoAttachmentViewHolder ->
                holder.bind(dataSet[position] as VideoAttachmentUiModel)
            is UrlPreviewViewHolder ->
                holder.bind(dataSet[position] as UrlPreviewUiModel)
            is MessageAttachmentViewHolder ->
                holder.bind(dataSet[position] as MessageAttachmentUiModel)
            is AuthorAttachmentViewHolder ->
                holder.bind(dataSet[position] as AuthorAttachmentUiModel)
            is ColorAttachmentViewHolder ->
                holder.bind(dataSet[position] as ColorAttachmentUiModel)
            is GenericFileAttachmentViewHolder ->
                holder.bind(dataSet[position] as GenericFileAttachmentUiModel)
            is MessageReplyViewHolder ->
                holder.bind(dataSet[position] as MessageReplyUiModel)
        }
    }

    override fun getItemId(position: Int): Long {
        val model = dataSet[position]
        return when (model) {
            is MessageUiModel -> model.messageId.hashCode().toLong()
            is BaseFileAttachmentUiModel -> model.id
            is AuthorAttachmentUiModel -> model.id
            else -> return position.toLong()
        }
    }

    fun appendData(dataSet: List<BaseUiModel<*>>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(dataSet)
        notifyItemChanged(previousDataSetSize, dataSet.size)
    }

    fun prependData(dataSet: List<BaseUiModel<*>>) {
        val item = dataSet.indexOfFirst { newItem ->
            this.dataSet.indexOfFirst { it.messageId == newItem.messageId && it.viewType == newItem.viewType } > -1
        }
        if (item == -1) {
            this.dataSet.addAll(0, dataSet)
            notifyItemRangeInserted(0, dataSet.size)
        } else {
            dataSet.forEach { item ->
                val index = this.dataSet.indexOfFirst {
                    item.messageId == it.messageId && item.viewType == it.viewType
                }
                if (index > -1) {
                    this.dataSet[index] = item
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun updateItem(message: BaseUiModel<*>) {
        val index = dataSet.indexOfLast { it.messageId == message.messageId }
        val indexOfNext = dataSet.indexOfFirst { it.messageId == message.messageId }
        Timber.d("index: $index")
        if (index > -1) {
            dataSet[index] = message
            dataSet.forEachIndexed { index, viewModel ->
                if (viewModel.messageId == message.messageId) {
                    if (viewModel.nextDownStreamMessage == null) {
                        viewModel.reactions = message.reactions
                    }
                    notifyItemChanged(index)
                }
            }
            // Delete message only if current is a system message update, i.e.: Message Removed
            if (message.message.isSystemMessage() && indexOfNext > -1 && indexOfNext != index) {
                dataSet.removeAt(indexOfNext)
                notifyItemRemoved(indexOfNext)
            }
        }
    }

    fun removeItem(messageId: String) {
        val index = dataSet.indexOfFirst { it.messageId == messageId }
        if (index > -1) {
            val oldSize = dataSet.size
            val newSet = dataSet.filterNot { it.messageId == messageId }
            dataSet.clear()
            dataSet.addAll(newSet)
            val newSize = dataSet.size
            notifyItemRangeRemoved(index, oldSize - newSize)
        }
    }

    private val actionsListener = object : BaseViewHolder.ActionsListener {

        override fun isActionsEnabled(): Boolean = enableActions

        override fun onActionSelected(item: MenuItem, message: Message) {
            message.apply {
                when (item.itemId) {
                    R.id.action_message_reply -> {
                        if (roomName != null && roomType != null) {
                            presenter?.citeMessage(roomName, roomType, id, true)
                        }
                    }
                    R.id.action_message_quote -> {
                        if (roomName != null && roomType != null) {
                            presenter?.citeMessage(roomName, roomType, id, false)
                        }
                    }
                    R.id.action_message_copy -> {
                        presenter?.copyMessage(id)
                    }
                    R.id.action_message_edit -> {
                        presenter?.editMessage(roomId, id, message.message)
                    }
                    R.id.action_message_star -> {
                        if (!item.isChecked) {
                            presenter?.starMessage(id)
                        } else {
                            presenter?.unstarMessage(id)
                        }
                    }
                    R.id.action_message_unpin -> {
                        if (!item.isChecked) {
                            presenter?.pinMessage(id)
                        } else {
                            presenter?.unpinMessage(id)
                        }
                    }
                    R.id.action_message_delete -> presenter?.deleteMessage(roomId, id)
                    R.id.action_menu_msg_react -> presenter?.showReactions(id)
                    else -> TODO("Not implemented")
                }
            }
        }
    }
}