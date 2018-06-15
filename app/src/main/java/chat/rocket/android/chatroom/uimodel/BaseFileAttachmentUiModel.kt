package chat.rocket.android.chatroom.uimodel

interface BaseFileAttachmentUiModel<out T> : BaseAttachmentUiModel<T> {
    val attachmentTitle: CharSequence
    val id: Long
}