package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message

data class MessageViewModel(
        override val message: Message,
        override val rawData: Message,
        override val messageId: String,
        override val avatar: String,
        override val time: CharSequence,
        override val senderName: CharSequence,
        override val content: CharSequence,
        override val isPinned: Boolean,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        override var preview: Message? = null,
        var isFirstUnread: Boolean,
        override var isTemporary: Boolean = false
) : BaseMessageViewModel<Message> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.MESSAGE.viewType

    override val layoutId: Int
        get() = R.layout.item_message
}