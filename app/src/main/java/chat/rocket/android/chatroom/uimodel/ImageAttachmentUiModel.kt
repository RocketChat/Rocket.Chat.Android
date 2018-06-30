package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.ImageAttachment

data class ImageAttachmentUiModel(
        override val message: Message,
        override val rawData: ImageAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long,
        override var reactions: List<ReactionUiModel>,
        override var nextDownStreamMessage: BaseUiModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseFileAttachmentUiModel<ImageAttachment> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.IMAGE_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.message_attachment
}