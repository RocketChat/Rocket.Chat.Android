package chat.rocket.android.chatroom.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.*
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.util.extensions.openTabbedUrl
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ButtonAction
import chat.rocket.core.model.Message
import chat.rocket.core.model.isSystemMessage
import timber.log.Timber
import java.security.InvalidParameterException

class ChatRoomAdapter(
    private val roomId: String? = null,
    private val roomType: String? = null,
    private val roomName: String? = null,
    private val actionSelectListener: OnActionSelected? = null,
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
                    actionSelectListener?.openDirectMessage(roomName, permalink)
                }
            }
            BaseUiModel.ViewType.ACTIONS_ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_actions_attachment)
                ActionsAttachmentViewHolder(view, actionsListener, reactionListener, actionAttachmentOnClickListener)
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
            is ActionsAttachmentViewHolder ->
                holder.bind(dataSet[position] as ActionsAttachmentUiModel)
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

    fun clearData() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    fun appendData(dataSet: List<BaseUiModel<*>>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(dataSet)
        notifyItemChanged(previousDataSetSize, dataSet.size)
    }

    fun prependData(dataSet: List<BaseUiModel<*>>) {
        //---At first we will update all already saved elements with received updated ones
        val filteredDataSet = dataSet.filter { newItem ->
            val matchedIndex = this.dataSet.indexOfFirst { it.messageId == newItem.messageId && it.viewType == newItem.viewType }
            if (matchedIndex > -1) {
                this.dataSet[matchedIndex] = newItem
                notifyItemChanged(matchedIndex)
            }
            return@filter (matchedIndex < 0)
        }
        val minAdditionDate = filteredDataSet.minBy { it.message.timestamp } ?: return
        //---In the most cases we will just add new elements to the top of messages heap
        if (this.dataSet.isEmpty() || minAdditionDate.message.timestamp > this.dataSet[0].message.timestamp) {
            this.dataSet.addAll(0, filteredDataSet)
            notifyItemRangeInserted(0, filteredDataSet.size)
            return
        }
        //---Else branch: merging messages---
        //---We are inserting new received elements into set. Sort them by time+type and show
        if (filteredDataSet.isEmpty()) return
        this.dataSet.addAll(0, filteredDataSet)
        val tmp = this.dataSet.sortedWith(Comparator { t, t2 ->
            val timeComparison = t.message.timestamp.compareTo(t2.message.timestamp)
            if (timeComparison == 0) {
                return@Comparator t.viewType.compareTo(t2.viewType)
            }
            timeComparison
        }).reversed()
        this.dataSet.clear()
        this.dataSet.addAll(tmp)
        notifyDataSetChanged()
    }

    fun updateItem(message: BaseUiModel<*>): Boolean {
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
            return true
        }
        return false
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

    private val actionAttachmentOnClickListener = object : ActionAttachmentOnClickListener {
        override fun onActionClicked(view: View, action: Action) {
            val temp = action as ButtonAction
            if (temp.url != null && temp.isWebView != null) {
                if (temp.isWebView == true) {
                    //TODO: Open in a configurable sizable webview
                    Timber.d("Open in a configurable sizable webview")
                } else {
                    //Open in chrome custom tab
                    temp.url?.let { view.openTabbedUrl(it) }
                }
            } else if (temp.message != null && temp.isMessageInChatWindow != null) {
                if (temp.isMessageInChatWindow == true) {
                    //Send to chat window
                    temp.message?.let {
                        if (roomId != null) {
                            actionSelectListener?.sendMessage(roomId, it)
                        }
                    }
                } else {
                    //TODO: Send to bot but not in chat window
                    Timber.d("Send to bot but not in chat window")
                }
            }
        }
    }

    private val actionsListener = object : BaseViewHolder.ActionsListener {

        override fun isActionsEnabled(): Boolean = enableActions

        override fun onActionSelected(item: MenuItem, message: Message) {
            message.apply {
                when (item.itemId) {
                    R.id.action_message_info -> {
                        actionSelectListener?.showMessageInfo(id)
                    }
                    R.id.action_message_reply -> {
                        if (roomName != null && roomType != null) {
                            actionSelectListener?.citeMessage(roomName, roomType, id, true)
                        }
                    }
                    R.id.action_message_quote -> {
                        if (roomName != null && roomType != null) {
                            actionSelectListener?.citeMessage(roomName, roomType, id, false)
                        }
                    }
                    R.id.action_message_copy -> {
                        actionSelectListener?.copyMessage(id)
                    }
                    R.id.action_message_edit -> {
                        actionSelectListener?.editMessage(roomId, id, message.message)
                    }
                    R.id.action_message_star -> {
                        actionSelectListener?.toogleStar(id, !item.isChecked)
                    }
                    R.id.action_message_unpin -> {
                        actionSelectListener?.tooglePin(id, !item.isChecked)
                    }
                    R.id.action_message_delete -> {
                        actionSelectListener?.deleteMessage(roomId, id)
                    }
                    R.id.action_menu_msg_react -> {
                        actionSelectListener?.showReactions(id)
                    }
                    else -> {
                        TODO("Not implemented")
                    }
                }
            }
        }
    }

    interface OnActionSelected {
        fun showMessageInfo(id: String)
        fun citeMessage(roomName: String, roomType: String, messageId: String, mentionAuthor: Boolean)
        fun copyMessage(id: String)
        fun editMessage(roomId: String, messageId: String, text: String)
        fun toogleStar(id: String, star: Boolean)
        fun tooglePin(id: String, pin: Boolean)
        fun deleteMessage(roomId: String, id: String)
        fun showReactions(id: String)
        fun openDirectMessage(roomName: String, message: String)
        fun sendMessage(chatRoomId: String, text: String)
    }
}