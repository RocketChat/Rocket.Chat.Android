package chat.rocket.android.widget.autocompletion.strategy.regex

import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.strategy.CompletionStrategy
import java.util.concurrent.CopyOnWriteArrayList

internal class StringMatchingCompletionStrategy : CompletionStrategy {
    private val list = CopyOnWriteArrayList<SuggestionModel>()
    private val pinnedList = mutableListOf<SuggestionModel>()

    override fun autocompleteItems(prefix: String): List<SuggestionModel> {
        val partialResult = list.filter {
            it.searchList.forEach { word ->
                if (word.contains(prefix, ignoreCase = true)) {
                    return@filter true
                }
            }
            false
        }.sortedByDescending { it.pinned }
        val result = partialResult.take(5).toMutableList()
        result.addAll(pinnedList)
        return result.toList()
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