package chat.rocket.android.chatdetails.domain

data class Option(
    val name: String,
    val icon: Int,
    val listener: () -> Unit
)