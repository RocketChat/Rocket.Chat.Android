package chat.rocket.android.widget.autocompletion.repository

interface LocalSuggestionProvider {
    fun find(prefix: String)
}