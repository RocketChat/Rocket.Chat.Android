package chat.rocket.android.chatroom.viewmodel

interface BaseFileAttachmentViewModel<out T> : BaseAttachmentViewModel<T> {
    val attachmentTitle: CharSequence
    val id: Long
}