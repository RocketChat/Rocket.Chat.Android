package chat.rocket.android.chatrooms.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.util.extensions.inflate

class RoomsAdapter(private val listener: (RoomUiModel) -> Unit) :
    RecyclerView.Adapter<ViewHolder<*>>() {

    init {
        setHasStableIds(true)
    }

    var values: List<ItemHolder<*>> = ArrayList(0)
        set(items) {
            field = items
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> = when (viewType) {
        VIEW_TYPE_ROOM -> {
            val view = parent.inflate(R.layout.item_chat)
            RoomViewHolder(view, listener)
        }
        VIEW_TYPE_HEADER -> {
            val view = parent.inflate(R.layout.item_chatroom_header)
            HeaderViewHolder(view)
        }
        VIEW_TYPE_LOADING -> {
            val view = parent.inflate(R.layout.item_loading)
            LoadingViewHolder(view)
        }
        else -> throw IllegalStateException("View type must be either Room, Header or Loading")
    }

    override fun getItemCount() = values.size

    override fun getItemId(position: Int): Long {
        val item = values[position]
        return when (item) {
            is RoomItemHolder -> item.data.id.hashCode().toLong()
            is HeaderItemHolder -> item.data.hashCode().toLong()
            is LoadingItemHolder -> "loading".hashCode().toLong()
            else -> throw IllegalStateException("View type must be either Room, Header or Loading")
        }
    }

    override fun getItemViewType(position: Int): Int = when (values[position]) {
        is RoomItemHolder -> VIEW_TYPE_ROOM
        is HeaderItemHolder -> VIEW_TYPE_HEADER
        is LoadingItemHolder -> VIEW_TYPE_LOADING
        else -> throw IllegalStateException("View type must be either Room, Header or Loading")
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (holder is RoomViewHolder) {
            holder.bind(values[position] as RoomItemHolder)
        } else if (holder is HeaderViewHolder) {
            holder.bind(values[position] as HeaderItemHolder)
        }
    }

    companion object {
        const val VIEW_TYPE_ROOM = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_LOADING = 3
    }
}