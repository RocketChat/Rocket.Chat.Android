package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.attachment.AudioAttachment

data class AudioAttachmentViewModel(
        override val rawData: AudioAttachment,
        override val messageId: String,
        override val attachmentUrl: String,
        override val attachmentTitle: CharSequence,
        override val id: Long
) : BaseFileAttachmentViewModel<AudioAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.AUDIO_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.message_attachment
}