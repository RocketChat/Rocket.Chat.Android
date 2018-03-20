package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message

data class MessageAttachmentViewModel(
        override val message: Message,
        override val rawData: Message,
        override val messageId: String,
        var senderName: String,
        val time: CharSequence,
        val content: CharSequence,
        val isPinned: Boolean,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        var messageLink: String? = null,
        override var preview: Message? = null
) : BaseViewModel<Message> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.MESSAGE_ATTACHMENT.viewType

    override val layoutId: Int
        get() = R.layout.item_message_attachment
}