package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.ColorAttachment

data class ColorAttachmentViewModel(
    override val attachmentUrl: String,
    val id: Long,
    val color: Int,
    val text: CharSequence,
    override val message: Message,
    override val rawData: ColorAttachment,
    override val messageId: String,
    override var reactions: List<ReactionViewModel>,
    override var nextDownStreamMessage: BaseViewModel<*>? = null,
    override var preview: Message? = null,
    override var isTemporary: Boolean = false
) : BaseAttachmentViewModel<ColorAttachment> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.COLOR_ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_color_attachment
}