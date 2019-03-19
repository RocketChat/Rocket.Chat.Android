package chat.rocket.android.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.contacts.models.Contact

class SelectedContactsAdapter(private val list: ArrayList<Contact>, private val removeClickListener: (Contact) -> Unit)
    : RecyclerView.Adapter<SelectedContactsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedContactsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SelectedContactsViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SelectedContactsViewHolder, position: Int) {
        val contact: Contact = list[position]
        holder.bind(contact, removeClickListener)
    }

    override fun getItemCount(): Int = list.size

}
