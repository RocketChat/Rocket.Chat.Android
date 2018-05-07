package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.GenericFileAttachment
import chat.rocket.core.model.attachment.ImageAttachment

data class GenericFileAttachmentViewModel(
        override val message: Message,
        override val rawData: GenericFileAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseFileAttachmentViewModel<GenericFileAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.GENERIC_FILE_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_file_attachment
}