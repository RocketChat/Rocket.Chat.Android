package chat.rocket.android.widget.autocompletion.ui

import android.support.v7.widget.RecyclerView
import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.strategy.CompletionStrategy
import chat.rocket.android.widget.autocompletion.strategy.regex.StringMatchingCompletionStrategy
import java.lang.reflect.Type
import kotlin.properties.Delegates

abstract class SuggestionsAdapter<VH : BaseSuggestionViewHolder>(val token: String) : RecyclerView.Adapter<VH>() {
    private val strategy: CompletionStrategy = StringMatchingCompletionStrategy()
    private var itemType: Type? = null
    private var itemClickListener: ItemClickListener? = null
    private var providerExternal: ((query: String) -> Unit)? = null
    private var prefix: String by Delegates.observable("", { _, _, _ ->
        strategy.autocompleteItems(prefix)
        notifyItemRangeChanged(0, 5)
    })

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).text.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    override fun getItemCount() = strategy.autocompleteItems(prefix).size

    private fun getItem(position: Int): SuggestionModel {
        return strategy.autocompleteItems(prefix)[position]
    }

    fun autocomplete(prefix: String) {
        this.prefix = prefix.toLowerCase().trim()
    }

    fun addItems(list: List<SuggestionModel>) {
        strategy.addAll(list)
        // Since we've just added new items we should check for possible new completion suggestions.
        strategy.autocompleteItems(prefix)
        notifyItemRangeChanged(0, 5)
    }

    fun setOnClickListener(clickListener: ItemClickListener) {
        this.itemClickListener = clickListener
    }

    fun hasItemClickListener() = itemClickListener != null

    fun prefix() = prefix

    fun cancel() {
        strategy.addAll(emptyList())
        strategy.autocompleteItems(prefix)
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onClick(item: SuggestionModel)
    }
}