package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.CommandSuggestionsAdapter.CommandSuggestionsViewHolder
import chat.rocket.android.chatroom.viewmodel.suggestion.CommandSuggestionViewModel
import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.ui.BaseSuggestionViewHolder
import chat.rocket.android.widget.autocompletion.ui.SuggestionsAdapter

class CommandSuggestionsAdapter : SuggestionsAdapter<CommandSuggestionsViewHolder>("/", UNLIMITED_RESULT_COUNT) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandSuggestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_command_item, parent,
                false)
        return CommandSuggestionsViewHolder(view)
    }

    class CommandSuggestionsViewHolder(view: View) : BaseSuggestionViewHolder(view) {

        override fun bind(item: SuggestionModel, itemClickListener: SuggestionsAdapter.ItemClickListener?) {
            item as CommandSuggestionViewModel
            with(itemView) {
                val nameTextView = itemView.findViewById<TextView>(R.id.text_command_name)
                val descriptionTextView = itemView.findViewById<TextView>(R.id.text_command_description)
                nameTextView.text = item.text
                descriptionTextView.text = item.description.replace("_", " ")
                setOnClickListener {
                    itemClickListener?.onClick(item)
                }
            }
        }
    }
}