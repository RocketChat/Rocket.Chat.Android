package chat.rocket.android.contacts.adapter

import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.*
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extensions.inflate
import kotlin.collections.HashMap


class ContactRecyclerViewAdapter(
        private val context: MainActivity,
        private val contactArrayList: List<ItemHolder<*>>,
        private val contactHashMap: HashMap<String, String>


) : RecyclerView.Adapter<ViewHolder<*>>() {

    init {
        setHasStableIds(true)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        return when (viewType) {
            VIEW_TYPE_CONTACT -> {
                val view = parent.inflate(R.layout.item_contact)
                ContactViewHolder(view)
            }
            VIEW_TYPE_HEADER -> {
                val view = parent.inflate(R.layout.item_heading)
                ContactHeaderViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = parent.inflate(R.layout.item_loading)
                 LoadingViewHolder(view)
            }
            VIEW_TYPE_INVITE -> {
                val view = parent.inflate(R.layout.item_invite)
                InviteViewHolder(view)
            }
            else -> throw IllegalStateException("View type must be either Room, Header or Loading")
        }
    }

    override fun getItemCount() = contactArrayList.size

    override fun getItemId(position: Int): Long {
        val item = contactArrayList[position]
        return when (item) {
            is ContactItemHolder -> item.data.hashCode().toLong()
            is ContactHeaderItemHolder -> item.data.hashCode().toLong()
            is LoadingItemHolder -> "loading".hashCode().toLong()
            is inviteItemHolder -> item.data.hashCode().toLong()
            else -> throw IllegalStateException("View type must be either Room, Header or Loading")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (contactArrayList[position]) {
            is ContactItemHolder -> VIEW_TYPE_CONTACT
            is ContactHeaderItemHolder -> VIEW_TYPE_HEADER
            is LoadingItemHolder -> VIEW_TYPE_LOADING
            is inviteItemHolder -> VIEW_TYPE_INVITE
            else -> throw IllegalStateException("View type must be either Room, Header or Loading")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (holder is ContactViewHolder) {
            holder.bind(contactArrayList[position] as ContactItemHolder)
            holder.itemView.setOnClickListener {
                //  it.context.startActivity(Intent.createChooser(this, context.getString(R.string.msg_share_using)))

            }

        } else if (holder is ContactHeaderViewHolder) {
            holder.bind(contactArrayList[position] as ContactHeaderItemHolder)
        } else if (holder is InviteViewHolder) {
            holder.bind(contactArrayList[position] as inviteItemHolder)
            holder.itemView.setOnClickListener {
                shareApp();
            }

        }
    }


    private fun shareApp() {
        with(Intent(Intent.ACTION_SEND)) {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.msg_check_this_out))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.play_store_link))
            context.startActivity(Intent.createChooser(this, context.getString(R.string.msg_share_using)))
        }
    }

    companion object {
        const val VIEW_TYPE_CONTACT = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_LOADING = 3
        const val VIEW_TYPE_INVITE = 4

    }

}