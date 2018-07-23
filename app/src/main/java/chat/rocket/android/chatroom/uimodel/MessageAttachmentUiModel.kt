package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.core.model.Message

data class MessageAttachmentUiModel(
    override val message: Message,
    override val rawData: Message,
    override val messageId: String,
    var senderName: String?,
    val time: CharSequence?,
    val content: CharSequence,
    val isPinned: Boolean,
    override var reactions: List<ReactionUiModel>,
    override var nextDownStreamMessage: BaseUiModel<*>? = null,
    var messageLink: String? = null,
    override var preview: Message? = null,
    override var isTemporary: Boolean = false,
    override var unread: Boolean? = null,
    override var menuItemsToHide: MutableList<Int> = mutableListOf(),
    override var currentDayMarkerText: String,
    override var showDayMarker: Boolean
) : BaseUiModel<Message> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.MESSAGE_ATTACHMENT.viewType

    override val layoutId: Int
        get() = R.layout.item_message_attachment
}