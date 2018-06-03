package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.android.chatroom.domain.MessageReply
import chat.rocket.core.model.Message

data class MessageReplyViewModel(
    override val rawData: MessageReply,
    override val messageId: String,
    override var reactions: List<ReactionViewModel>,
    override var nextDownStreamMessage: BaseViewModel<*>?,
    override var preview: Message?,
    override var isTemporary: Boolean = false,
    override val message: Message,
    override var unread: Boolean? = null
) : BaseViewModel<MessageReply> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.MESSAGE_REPLY.viewType
    override val layoutId: Int
        get() = R.layout.item_message_reply
}