package chat.rocket.android.suggestions.strategy.regex

import chat.rocket.android.suggestions.model.SuggestionModel
import chat.rocket.android.suggestions.strategy.CompletionStrategy
import chat.rocket.android.suggestions.ui.SuggestionsAdapter.Companion.RESULT_COUNT_UNLIMITED
import java.util.concurrent.CopyOnWriteArrayList

internal class StringMatchingCompletionStrategy(private val threshold: Int = RESULT_COUNT_UNLIMITED) : CompletionStrategy {
    private val list = CopyOnWriteArrayList<SuggestionModel>()
    private val pinnedList = mutableListOf<SuggestionModel>()

    init {
        check(threshold >= RESULT_COUNT_UNLIMITED)
    }

    override fun autocompleteItems(prefix: String): List<SuggestionModel> {
        val partialResult = list.filter {
            it.searchList.forEach { word ->
                if (word.contains(prefix, ignoreCase = true)) {
                    return@filter true
                }
            }
            false
        }.sortedByDescending { it.pinned }
        return if (threshold == RESULT_COUNT_UNLIMITED)
            partialResult.toList()
        else {
            val result = partialResult.take(threshold).toMutableList()
            result.addAll(pinnedList)
            result.toList()
        }
    }

    override fun addAll(list: List<SuggestionModel>) {
        this.list.addAllAbsent(list)
    }

    override fun addPinned(list: List<SuggestionModel>) {
        this.pinnedList.addAll(list)
    }

    override fun getItem(prefix: String, position: Int): SuggestionModel {
        return list[position]
    }

    override fun size(): Int {
        return list.size
    }
}
