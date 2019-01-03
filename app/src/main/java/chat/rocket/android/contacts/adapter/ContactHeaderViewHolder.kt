package chat.rocket.android.contacts.adapter

import android.view.View
import chat.rocket.android.chatrooms.adapter.ViewHolder
import chat.rocket.android.contacts.adapter.ContactHeaderItemHolder
import kotlinx.android.synthetic.main.item_heading.view.*

class ContactHeaderViewHolder(itemView: View) : ViewHolder<ContactHeaderItemHolder>(itemView) {
    override fun bindViews(data: ContactHeaderItemHolder) {
        with(itemView) {
            contacts_heading.text = data.data
        }
    }
}
