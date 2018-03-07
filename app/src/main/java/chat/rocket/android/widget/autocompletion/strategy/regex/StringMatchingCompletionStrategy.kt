package chat.rocket.android.widget.autocompletion.strategy.regex

import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.strategy.CompletionStrategy
import java.util.concurrent.CopyOnWriteArrayList

internal class StringMatchingCompletionStrategy : CompletionStrategy {
    private val list = CopyOnWriteArrayList<SuggestionModel>()

    override fun autocompleteItems(prefix: String): List<SuggestionModel> {
        return list.filter {
            it.searchList.forEach { word ->
                if (word.contains(prefix, ignoreCase = true)) {
                    return@filter true
                }
            }
            false
        }.sortedByDescending { it.pinned }.take(5)
    }

    override fun addAll(list: List<SuggestionModel>) {
//        this.list.removeAll { !it.pinned }
        this.list.addAllAbsent(list)
    }

    override fun getItem(prefix: String, position: Int): SuggestionModel {
        return list[position]
    }

    override fun size(): Int {
        return list.size
    }
}