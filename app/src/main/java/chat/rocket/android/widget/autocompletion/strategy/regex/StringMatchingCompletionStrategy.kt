package chat.rocket.android.widget.autocompletion.strategy.regex

import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.strategy.CompletionStrategy
import chat.rocket.android.widget.autocompletion.ui.SuggestionsAdapter
import java.util.concurrent.CopyOnWriteArrayList

internal class StringMatchingCompletionStrategy(private val threshold: Int = -1) : CompletionStrategy {
    private val list = CopyOnWriteArrayList<SuggestionModel>()

    override fun autocompleteItems(prefix: String): List<SuggestionModel> {
        val result = list.filter {
            it.searchList.forEach { word ->
                if (word.contains(prefix, ignoreCase = true)) {
                    return@filter true
                }
            }
            false
        }.sortedByDescending { it.pinned }
        return if (threshold == SuggestionsAdapter.UNLIMITED_RESULT_COUNT) result else result.take(threshold)
    }

    override fun addAll(list: List<SuggestionModel>) {
        this.list.addAllAbsent(list)
    }

    override fun getItem(prefix: String, position: Int): SuggestionModel {
        return list[position]
    }

    override fun size(): Int {
        return list.size
    }
}