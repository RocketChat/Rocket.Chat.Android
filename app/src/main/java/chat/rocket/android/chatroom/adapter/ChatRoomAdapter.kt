package chat.rocket.android.chatroom.adapter

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.uimodel.AttachmentUiModel
import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.chatroom.uimodel.MessageReplyUiModel
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.chatroom.uimodel.UrlPreviewUiModel
import chat.rocket.android.chatroom.uimodel.toViewType
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.openTabbedUrl
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ButtonAction
import chat.rocket.core.model.isSystemMessage
import timber.log.Timber
import java.security.InvalidParameterException

class ChatRoomAdapter(
    private val roomId: String? = null,
    private val roomType: String? = null,
    private val roomName: String? = null,
    private val actionSelectListener: OnActionSelected? = null,
    private val enableActions: Boolean = true,
    private val reactionListener: EmojiReactionListener? = null,
    private val navigator: ChatRoomNavigator? = null,
    private val analyticsManager: AnalyticsManager? = null
) : RecyclerView.Adapter<BaseViewHolder<*>>() {
    private val dataSet = ArrayList<BaseUiModel<*>>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType.toViewType()) {
            BaseUiModel.ViewType.MESSAGE -> {
                val view = parent.inflate(R.layout.item_message)
                MessageViewHolder(
                    view,
                    actionsListener,
                    reactionListener,
                    { userId -> navigator?.toUserDetails(userId) },
                    {
                        if (roomId != null && roomType != null) {
                            navigator?.toVideoConference(roomId, roomType)
                        }
                    }
                )
            }
            BaseUiModel.ViewType.URL_PREVIEW -> {
                val view = parent.inflate(R.layout.message_url_preview)
                UrlPreviewViewHolder(view, actionsListener, reactionListener)
            }
            BaseUiModel.ViewType.ATTACHMENT -> {
                val view = parent.inflate(R.layout.item_message_attachment)
                AttachmentViewHolder(
                    view,
                    actionsListener,
                    reactionListener,
                    actionAttachmentOnClickListener
                )
            }
            BaseUiModel.ViewType.MESSAGE_REPLY -> {
                val view = parent.inflate(R.layout.item_message_reply)
                MessageReplyViewHolder(
                    view,
                    actionsListener,
                    reactionListener
                ) { roomName, permalink ->
                    actionSelectListener?.openDirectMessage(roomName, permalink)
                }
            }
            else -> {
                throw InvalidParameterException("TODO - implement for ${viewType.toViewType()}")
            }
        }
    }

    override fun getItemViewType(position: Int): Int = dataSet[position].viewType

    override fun getItemCount(): Int = dataSet.size

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
            is UrlPreviewViewHolder -> {
                holder.bind(dataSet[position] as UrlPreviewUiModel)
            }
            is MessageReplyViewHolder ->
                holder.bind(dataSet[position] as MessageReplyUiModel)
            is AttachmentViewHolder ->
                holder.bind(dataSet[position] as AttachmentUiModel)
        }
    }

    override fun getItemId(position: Int): Long {
        val model = dataSet[position]
        return when (model) {
            is MessageUiModel -> model.messageId.hashCode().toLong()
            is AttachmentUiModel -> model.id
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
            val matchedIndex =
                this.dataSet.indexOfFirst { it.messageId == newItem.messageId && it.viewType == newItem.viewType }
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
            dataSet.forEachIndexed { ind, viewModel ->
                if (viewModel.messageId == message.messageId) {
                    if (viewModel.nextDownStreamMessage == null) {
                        viewModel.reactions = message.reactions
                    }
                    notifyItemChanged(ind)
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
            if (analyticsManager != null && roomName != null && roomType != null && actionSelectListener != null) {
                with(message) {
                    when (item.itemId) {
                        R.id.action_info -> {
                            actionSelectListener.showMessageInfo(id)
                            analyticsManager.logMessageActionInfo()
                        }

                        R.id.action_reply -> {
                            actionSelectListener.citeMessage(roomName, roomType, id, true)
                            analyticsManager.logMessageActionReply()
                        }

                        R.id.action_quote -> {
                            actionSelectListener.citeMessage(roomName, roomType, id, false)
                            analyticsManager.logMessageActionQuote()
                        }

                        R.id.action_copy -> {
                            actionSelectListener.copyMessage(id)
                            analyticsManager.logMessageActionCopy()
                        }

                        R.id.action_edit -> {
                            actionSelectListener.editMessage(roomId, id, this.message)
                            analyticsManager.logMessageActionEdit()
                        }

                        R.id.action_star -> {
                            actionSelectListener.toggleStar(id, !item.isChecked)
                            analyticsManager.logMessageActionStar()
                        }

                        R.id.action_pin -> {
                            actionSelectListener.togglePin(id, !item.isChecked)
                            analyticsManager.logMessageActionPin()
                        }

                        R.id.action_delete -> {
                            actionSelectListener.deleteMessage(roomId, id)
                            analyticsManager.logMessageActionDelete()
                        }

                        R.id.action_add_reaction -> {
                            actionSelectListener.showReactions(id)
                            analyticsManager.logMessageActionAddReaction()
                        }

                        R.id.action_permalink -> {
                            actionSelectListener.copyPermalink(id)
                            analyticsManager.logMessageActionPermalink()
                        }

                        R.id.action_report -> {
                            actionSelectListener.reportMessage(id)
                            analyticsManager.logMessageActionReport()
                        }
                    }
                }
            }
        }
    }

    interface OnActionSelected {

        fun showMessageInfo(id: String)

        fun citeMessage(
            roomName: String,
            roomType: String,
            messageId: String,
            mentionAuthor: Boolean
        )

        fun copyMessage(id: String)

        fun editMessage(roomId: String, messageId: String, text: String)

        fun toggleStar(id: String, star: Boolean)

        fun togglePin(id: String, pin: Boolean)

        fun deleteMessage(roomId: String, id: String)

        fun showReactions(id: String)

        fun openDirectMessage(roomName: String, message: String)

        fun sendMessage(chatRoomId: String, text: String)

        fun copyPermalink(id: String)

        fun reportMessage(id: String)
    }
}
