package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.android.chatroom.domain.MessageReply
import chat.rocket.core.model.Message

data class MessageReplyUiModel(
    override val rawData: MessageReply,
    override val messageId: String,
    override var reactions: List<ReactionUiModel>,
    override var nextDownStreamMessage: BaseUiModel<*>?,
    override var preview: Message?,
    override var isTemporary: Boolean = false,
    override val message: Message,
    override var unread: Boolean? = null,
    override var menuItemsToHide: MutableList<Int> = mutableListOf(),
    override var currentDayMarkerText: String,
    override var showDayMarker: Boolean,
    override var permalink: String
) : BaseUiModel<MessageReply> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.MESSAGE_REPLY.viewType
    override val layoutId: Int
        get() = R.layout.item_message_reply
}