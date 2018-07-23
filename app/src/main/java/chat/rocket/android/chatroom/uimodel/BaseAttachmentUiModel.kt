package chat.rocket.android.chatroom.uimodel

interface BaseAttachmentUiModel<out T> : BaseUiModel<T> {
    val attachmentUrl: String
}