package chat.rocket.android.emoji

data class Emoji(
    val shortname: String,
    val shortnameAlternates: List<String>,
    val unicode: String,
    val keywords: List<String>,
    val category: String,
    val count: Int = 0,
    val siblings: MutableCollection<Emoji> = mutableListOf(),
    val fitzpatrick: Fitzpatrick = Fitzpatrick.Default
)