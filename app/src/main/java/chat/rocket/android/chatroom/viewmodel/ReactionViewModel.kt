package chat.rocket.android.chatroom.viewmodel

data class ReactionViewModel(
        val messageId: String,
        val shortname: String,
        val unicode: CharSequence,
        val count: Int,
        val usernames: List<String> = emptyList()
)