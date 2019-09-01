package chat.rocket.android.push

data class PushMessage(
    val title: String,
    val message: String,
    val info: PushInfo,
    val image: String? = null,
    val count: String? = null,
    val notificationId: String,
    val summaryText: String? = null,
    val style: String? = null
)
