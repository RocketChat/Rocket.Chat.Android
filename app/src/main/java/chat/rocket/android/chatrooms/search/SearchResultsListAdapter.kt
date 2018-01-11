package chat.rocket.android.chatrooms.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R


class SearchResultsListAdapter(private var data: List<ChatRoomSuggestion>) : RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.getContext())
                .inflate(R.layout.search_results_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val chatRoomSuggestion = data[position]
        holder?.chatRoomName?.text = chatRoomSuggestion.body
    }

    override fun getItemCount(): Int = data.size

    fun swapData(newData: List<ChatRoomSuggestion>) {
        data = newData
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatRoomName: TextView = view.findViewById(R.id.text_chat_suggestion_name)
    }
}