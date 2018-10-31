package chat.rocket.android.chatroom.uimodel

import chat.rocket.android.R
import chat.rocket.core.model.Message
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.actions.Action

data class AttachmentUiModel(
        override val message: Message,
        override val rawData: Attachment,
        override val messageId: String,
        override var reactions: List<ReactionUiModel>,
        override var nextDownStreamMessage: BaseUiModel<*>? = null,
        override var preview: Message?,
        override var isTemporary: Boolean,
        override var unread: Boolean?,
        override var currentDayMarkerText: String,
        override var showDayMarker: Boolean,
        override var menuItemsToHide: MutableList<Int> = mutableListOf(),
        override var permalink: String,
        val id: Long,
        val title: CharSequence?,
        val description: CharSequence?,
        val authorName: CharSequence?,
        val text: CharSequence?,
        val color: Int?,
        val imageUrl: String?,
        val videoUrl: String?,
        val audioUrl: String?,
        val titleLink: String?,
        val messageLink: String?,
        val type: String?,
        // TODO - attachments
        val timestamp: CharSequence?,
        val authorIcon: String?,
        val authorLink: String?,
        val fields: CharSequence?,
        val buttonAlignment: String?,
        val actions: List<Action>?
) : BaseUiModel<Attachment> {
    override val viewType: Int
        get() = BaseUiModel.ViewType.ATTACHMENT.viewType
    override val layoutId: Int
        get() = R.layout.item_message_attachment

    val hasTitle: Boolean
        get() = !title.isNullOrEmpty()
    val hasDescription: Boolean
        get() = !description.isNullOrEmpty()
    val hasText: Boolean
        get() = !text.isNullOrEmpty()
    val hasImage: Boolean
        get() = imageUrl.orEmpty().isNotEmpty()
    val hasVideo: Boolean
        get() = videoUrl.orEmpty().isNotEmpty()
    val hasAudio: Boolean
        get() = audioUrl.orEmpty().isNotEmpty()
    val hasAudioOrVideo: Boolean
        get() = hasAudio || hasVideo
    val hasFile: Boolean
        get() = type.orEmpty().contentEquals("file") && titleLink.orEmpty().isNotEmpty()
    val hasTitleLink: Boolean
        get() = titleLink.orEmpty().isNotEmpty()
    val hasMedia: Boolean
        get() = hasImage || hasAudioOrVideo || hasFile
    val hasMessage: Boolean
        get() = messageLink.orEmpty().isNotEmpty()
    val hasAuthorName: Boolean
        get() = !authorName.isNullOrEmpty()
    val hasAuthorLink: Boolean
        get() = authorLink.orEmpty().isNotEmpty()
    val hasAuthorIcon: Boolean
        get() = authorIcon.orEmpty().isNotEmpty()
    val hasFields: Boolean
        get() = !fields.isNullOrEmpty()
    val hasActions: Boolean
        get() = actions != null && actions.isNotEmpty()
}
