package chat.rocket.android.emoji

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Emoji(
    @PrimaryKey
    var shortname: String = "",
    var shortnameAlternates: List<String> = listOf(),
    var unicode: String = "",
    @Ignore val keywords: List<String> = listOf(),
    var category: String = "",
    var count: Int = 0,
    var siblings: MutableList<String> = mutableListOf(), // Siblings are the same emoji with different skin tones.
    var fitzpatrick: String = Fitzpatrick.Default.type,
    var url: String? = null, // Filled for custom emojis
    var isDefault: Boolean = true // Tell if this is the default emoji if it has siblings (usually a yellow-toned one).
)
