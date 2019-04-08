package chat.rocket.android.chatroom.uimodel

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
    override var currentDayMarkerText: String,
    override var showDayMarker: Boolean,
    override var reactions: List<ReactionUiModel>,
    override var nextDownStreamMessage: BaseUiModel<*>? = null,
    override var preview: Message? = null,
    override var unread: Boolean? = null,
    var isFirstUnread: Boolean,
    override var isTemporary: Boolean = false,
    override var menuItemsToHide: MutableList<Int> = mutableListOf(),
    override var permalink: String,
    val subscriptionId: String
) : BaseMessageUiModel<Message> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.MESSAGE.viewType
    override val layoutId: Int
        get() = R.layout.item_message
}