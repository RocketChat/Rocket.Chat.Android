package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.AuthorAttachment

data class AuthorAttachmentUiModel(
    override val attachmentUrl: String,
    val id: Long,
    val name: CharSequence?,
    val icon: String?,
    val fields: CharSequence?,
    override val message: Message,
    override val rawData: AuthorAttachment,
    override val messageId: String,
    override var reactions: List<ReactionUiModel>,
    override var nextDownStreamMessage: BaseUiModel<*>? = null,
    override var preview: Message? = null,
    override var isTemporary: Boolean = false,
    override var unread: Boolean? = null,
    override var menuItemsToHide: MutableList<Int> = mutableListOf(),
    override var currentDayMarkerText: String,
    override var showDayMarker: Boolean
) : BaseAttachmentUiModel<AuthorAttachment> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.AUTHOR_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_author_attachment
}