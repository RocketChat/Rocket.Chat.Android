package chat.rocket.android.chatroom.models.messages

import chat.rocket.android.R
import chat.rocket.core.model.Message

data class MessageUiModel(
    override val message: Message,
    override val rawData: Message,
    override val messageId: String,
    override val avatar: String,
    override val time: CharSequence,
    override val senderName: CharSequence,
    override val content: CharSequence,
    override val isPinned: Boolean,
    val attachments: Boolean
) : BaseMessageUiModel<Message> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.MESSAGE.viewType

    override val layoutId: Int
        get() = R.layout.item_message
}