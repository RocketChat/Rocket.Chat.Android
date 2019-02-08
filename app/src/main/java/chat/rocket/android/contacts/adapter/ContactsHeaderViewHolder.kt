package chat.rocket.android.contacts.adapter

import android.view.View
import chat.rocket.android.chatrooms.adapter.ViewHolder
import kotlinx.android.synthetic.main.item_heading.view.*

class ContactsHeaderViewHolder(itemView: View) : ViewHolder<ContactsHeaderItemHolder>(itemView) {
    override fun bindViews(data: ContactsHeaderItemHolder) {
        with(itemView) {
            contacts_heading.text = data.data
        }
    }
}
