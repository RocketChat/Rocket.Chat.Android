package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.AuthorAttachment

data class AuthorAttachmentViewModel(
        override val attachmentUrl: String,
        val id: Long,
        val name: CharSequence?,
        val icon: String?,
        val fields: CharSequence?,
        override val message: Message,
        override val rawData: AuthorAttachment,
        override val messageId: String,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseAttachmentViewModel<AuthorAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.AUTHOR_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_author_attachment
}