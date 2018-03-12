package chat.rocket.android.chatroom.viewmodel

data class ReactionViewModel(
        val messageId: String,
        val shortname: CharSequence,
        val count: Int,
        val usernames: List<String> = emptyList()
)