package chat.rocket.android.chatroom.models.messages

interface BaseMessageUiModel<out T> : BaseUiModel<T> {
    val avatar: String
    val time: CharSequence
    val senderName: CharSequence
    val content: CharSequence
    val isPinned: Boolean
}