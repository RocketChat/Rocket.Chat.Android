package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.attachment.VideoAttachment

data class VideoAttachmentViewModel(
        override val rawData: VideoAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long
) : BaseFileAttachmentViewModel<VideoAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.VIDEO_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.message_attachment
}