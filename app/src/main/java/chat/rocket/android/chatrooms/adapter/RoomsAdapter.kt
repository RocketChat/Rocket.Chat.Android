package chat.rocket.android.chatrooms.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate

class RoomsAdapter(private val listener: (String) -> Unit) : RecyclerView.Adapter<ViewHolder<*>>() {

    init {
        setHasStableIds(true)
    }

    var values: List<ItemHolder<*>> = ArrayList(0)
        set(items) {
            field = items
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        if (viewType == 0) {
            val view = parent.inflate(R.layout.item_chat)
            return RoomViewHolder(view, listener)
        } else if (viewType == 1) {
            val view = parent.inflate(R.layout.item_chatroom_header)
            return HeaderViewHolder(view)
        }
        throw IllegalStateException("View type must be either Room or Header")
    }

    override fun getItemCount() = values.size

    override fun getItemId(position: Int): Long {
        val item = values[position]
        return when(item) {
            is HeaderItemHolder -> item.data.hashCode().toLong()
            is RoomItemHolder -> item.data.id.hashCode().toLong()
            else -> throw IllegalStateException("View type must be either Room or Header")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(values[position]) {
            is RoomItemHolder -> 0
            is HeaderItemHolder -> 1
            else -> throw IllegalStateException("View type must be either Room or Header")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (holder is RoomViewHolder) {
            holder.bind(values[position] as RoomItemHolder)
        } else if (holder is HeaderViewHolder) {
            holder.bind(values[position] as HeaderItemHolder)
        }
    }

}