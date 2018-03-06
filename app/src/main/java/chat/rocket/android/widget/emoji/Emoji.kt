package chat.rocket.android.widget.emoji

data class Emoji(
        val shortname: String,
        val shortnameAlternates: List<String>,
        val unicode: String,
        val keywords: List<String>,
        val category: String,
        val count: Int = 0
)