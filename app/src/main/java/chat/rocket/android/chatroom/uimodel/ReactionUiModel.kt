package chat.rocket.android.chatroom.uimodel

data class ReactionUiModel(
    val messageId: String,
    val shortname: String,
    val unicode: CharSequence,
    val count: Int,
    val usernames: List<String> = emptyList(),
    var url: String? = null,
    val isCustom: Boolean = false
)
