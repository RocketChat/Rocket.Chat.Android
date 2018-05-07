package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.VideoAttachment

data class VideoAttachmentViewModel(
        override val message: Message,
        override val rawData: VideoAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long,
        override var reactions: List<ReactionViewModel>,
        override var nextDownStreamMessage: BaseViewModel<*>? = null,
        override var preview: Message? = null,
        override var isTemporary: Boolean = false
) : BaseFileAttachmentViewModel<VideoAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.VIDEO_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.message_attachment
}