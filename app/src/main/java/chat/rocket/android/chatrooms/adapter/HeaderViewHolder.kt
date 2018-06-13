package chat.rocket.android.chatrooms.adapter

import android.view.View
import kotlinx.android.synthetic.main.item_chatroom_header.view.*

class HeaderViewHolder(itemView: View) : ViewHolder<HeaderItemHolder>(itemView) {
    override fun bindViews(data: HeaderItemHolder) {
        with(itemView) {
            text_chatroom_header.text = data.data
        }
    }
}