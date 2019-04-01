package chat.rocket.android.contacts.adapter

import android.view.View
import chat.rocket.android.chatrooms.adapter.ViewHolder
import kotlinx.android.synthetic.main.item_contact_action.view.*

class ContactsActionViewHolder(itemView: View) : ViewHolder<ContactsActionItemHolder>(itemView) {
    override fun bindViews(data: ContactsActionItemHolder) {
        with(itemView) {
            contact_action_title.text = data.data
        }
    }
}