package chat.rocket.android.suggestions.repository

interface LocalSuggestionProvider {
    fun find(prefix: String)
}