package chat.rocket.android.chatroom.viewmodel

interface BaseMessageViewModel<out T> : BaseViewModel<T> {
    val avatar: String
    val time: CharSequence
    val senderName: CharSequence
    val content: CharSequence
    val isPinned: Boolean
}