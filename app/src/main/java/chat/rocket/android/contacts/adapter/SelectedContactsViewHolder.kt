package chat.rocket.android.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.contacts.models.Contact
import com.facebook.drawee.view.SimpleDraweeView

class SelectedContactsViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_selected_contact, parent, false)) {
    private var name: TextView? = null
    private var avatar: SimpleDraweeView? = null
    private var removeButton: ImageView? = null

    init {
        name = itemView.findViewById(R.id.selected_contact_name)
        avatar = itemView.findViewById(R.id.selected_contact_image_avatar)
        removeButton = itemView.findViewById(R.id.remove_selected_contact)
    }

    fun bind(contact: Contact, removeClickListener: (Contact) -> Unit) {
        name?.text = contact.getName()?.substringBefore(" ")
        avatar?.setImageURI(contact.getAvatarUrl())
        removeButton?.setOnClickListener {
            removeClickListener(contact)
        }
    }

}