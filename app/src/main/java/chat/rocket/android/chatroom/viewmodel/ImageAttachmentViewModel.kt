package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.ImageAttachment

data class ImageAttachmentViewModel(
        override val message: Message,
        override val rawData: ImageAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseFileAttachmentViewModel<ImageAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.IMAGE_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.message_attachment
}