package chat.rocket.android.chatrooms.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.core.model.ChatRoom
import kotlinx.android.synthetic.main.item_chat_rooms.view.*

class ChatRoomsAdapter(private val chatRoom: List<ChatRoom>) :
    RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {
    private val singleChatRoom: List<ChatRoom>

    init {
        singleChatRoom = chatRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_chat_rooms,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return chatRoom.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(singleChatRoom[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            channel_name.text = chatRoom.name
        }
    }
}