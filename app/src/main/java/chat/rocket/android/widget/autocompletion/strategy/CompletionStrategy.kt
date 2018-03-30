package chat.rocket.android.widget.autocompletion.strategy

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

interface CompletionStrategy {
    fun getItem(prefix: String, position: Int): SuggestionModel
    fun autocompleteItems(prefix: String): List<SuggestionModel>
    fun addAll(list: List<SuggestionModel>)
    fun addPinned(list: List<SuggestionModel>)
    fun size(): Int
}