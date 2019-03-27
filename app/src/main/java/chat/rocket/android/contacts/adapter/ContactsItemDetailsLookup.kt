package chat.rocket.android.contacts.adapter

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class ContactsItemDetailsLookup (private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null && recyclerView.getChildViewHolder(view) is ContactsViewHolder) {
            return (recyclerView.getChildViewHolder(view) as ContactsViewHolder)
                    .getItemDetails()
        }
        return null
    }
}
