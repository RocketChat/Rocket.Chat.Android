package chat.rocket.android.suggestions.model

abstract class SuggestionModel(
    val text: String, // This is the text key for searches, must be unique.
    val searchList: List<String> = emptyList(),  // Where to search for matches.
    val pinned: Boolean = false /* If pinned item will have priority to show */
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SuggestionModel) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }
}
