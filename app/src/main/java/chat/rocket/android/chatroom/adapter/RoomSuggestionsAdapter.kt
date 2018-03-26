package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.RoomSuggestionsAdapter.RoomSuggestionsViewHolder
import chat.rocket.android.chatroom.viewmodel.suggestion.ChatRoomSuggestionViewModel
import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.ui.BaseSuggestionViewHolder
import chat.rocket.android.widget.autocompletion.ui.SuggestionsAdapter

class RoomSuggestionsAdapter : SuggestionsAdapter<RoomSuggestionsViewHolder>("#") {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomSuggestionsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_room_item, parent,
                false)
        return RoomSuggestionsViewHolder(view)
    }

    class RoomSuggestionsViewHolder(view: View) : BaseSuggestionViewHolder(view) {

        override fun bind(item: SuggestionModel, itemClickListener: SuggestionsAdapter.ItemClickListener?) {
            item as ChatRoomSuggestionViewModel
            with(itemView) {
                val fullname = itemView.findViewById<TextView>(R.id.text_fullname)
                val name = itemView.findViewById<TextView>(R.id.text_name)
                name.text = item.name
                fullname.text = item.fullName
                setOnClickListener {
                    itemClickListener?.onClick(item)
                }
            }
        }
    }
}