package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.CommandSuggestionsAdapter.CommandSuggestionsViewHolder
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.suggestions.model.SuggestionModel
import chat.rocket.android.suggestions.ui.BaseSuggestionViewHolder
import chat.rocket.android.suggestions.ui.SuggestionsAdapter

class CommandSuggestionsAdapter : SuggestionsAdapter<CommandSuggestionsViewHolder>(token = "/",
        constraint = CONSTRAINT_BOUND_TO_START, threshold = RESULT_COUNT_UNLIMITED) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandSuggestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_command_item, parent,
                false)
        return CommandSuggestionsViewHolder(view)
    }

    class CommandSuggestionsViewHolder(view: View) : BaseSuggestionViewHolder(view) {

        override fun bind(item: SuggestionModel, itemClickListener: SuggestionsAdapter.ItemClickListener?) {
            item as CommandSuggestionUiModel
            with(itemView) {
                val nameTextView = itemView.findViewById<TextView>(R.id.text_command_name)
                val descriptionTextView = itemView.findViewById<TextView>(R.id.text_command_description)
                nameTextView.text = "/${item.text}"
                val res = context.resources
                val id = res.getIdentifier(item.description, "string", context.packageName)
                val description = if (id > 0) res.getString(id) else ""
                descriptionTextView.text = description.toLowerCase()
                setOnClickListener {
                    itemClickListener?.onClick(item)
                }
            }
        }
    }
}