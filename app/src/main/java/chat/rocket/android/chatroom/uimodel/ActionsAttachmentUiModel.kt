package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ActionsAttachment

data class ActionsAttachmentUiModel(
        override val attachmentUrl: String,
        val title: String?,
        val actions: List<Action>,
        override val message: Message,
        override val rawData: ActionsAttachment,
        override val messageId: String,
        override var reactions: List<ReactionUiModel>,
        override var nextDownStreamMessage: BaseUiModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseAttachmentUiModel<ActionsAttachment> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.ACTIONS_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_actions_attachment
}