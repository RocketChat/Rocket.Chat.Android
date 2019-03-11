package chat.rocket.android.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.*
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.contacts.presentation.ContactsPresenter
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.common.model.UserPresence
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class ContactsRecyclerViewAdapter(
        private val context: MainActivity,
        private val presenter: ContactsPresenter,
        private val contactArrayList: List<ItemHolder<*>>
) : RecyclerView.Adapter<ViewHolder<*>>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        return when (viewType) {
            VIEW_TYPE_CONTACT -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_contact, parent, false)
                ContactsViewHolder(binding.root)
            }
            VIEW_TYPE_HEADER -> {
                val view = parent.inflate(R.layout.item_heading)
                ContactsHeaderViewHolder(view)
            }
            VIEW_TYPE_INVITE -> {
                val view = parent.inflate(R.layout.item_invite)
                InviteViewHolder(view)
            }
            VIEW_TYPE_PERMISSIONS -> {
                val view = parent.inflate(R.layout.item_permissions)
                PermissionsViewHolder(view)
            }
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite or Permissions")
        }
    }

    override fun getItemCount() = contactArrayList.size

    override fun getItemId(position: Int): Long {
        val item = contactArrayList[position]
        return when (item) {
            is ContactsItemHolder -> item.data.hashCode().toLong()
            is ContactsHeaderItemHolder -> item.data.hashCode().toLong()
            is InviteItemHolder -> item.data.hashCode().toLong()
            is PermissionsItemHolder -> item.data.hashCode().toLong()
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite or Permissions.")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (contactArrayList[position]) {
            is ContactsItemHolder -> VIEW_TYPE_CONTACT
            is ContactsHeaderItemHolder -> VIEW_TYPE_HEADER
            is InviteItemHolder -> VIEW_TYPE_INVITE
            is PermissionsItemHolder -> VIEW_TYPE_PERMISSIONS
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite or Permissions.")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (holder is ContactsViewHolder) {
            holder.bind(contactArrayList[position] as ContactsItemHolder)

            val contact: Contact = holder.data!!.data
            val userId = contact.getUserId()
            if (userId != null) {
                launch {
                    var userPresence: UserPresence? = presenter.getUserPresence(userId)
                    if (userPresence != null) {
                        contact.setStatus(userPresence.presence!!)
                    }
                    launch(UI) {
                        holder.setContactStatus(contact)
                    }
                }
                // Clicking the row will open the DM
                holder.itemView.setOnClickListener {
                    presenter.openDirectMessageChatRoom(contact.getUsername().toString())
                }
            }

            val inviteButton: Button = holder.itemView.findViewById(R.id.invite_contact)
            inviteButton.setOnClickListener { view ->
                run {
                    inviteButton.setText(R.string.Invited)
                    if (contact.isPhone()) {
                        presenter.inviteViaSMS(contact.getPhoneNumber()!!)
                    } else {
                        presenter.inviteViaEmail(contact.getEmailAddress()!!)
                    }
                }
            }
              // Clicking the @username button will open a DM
            val dmButton: Button = holder.itemView.findViewById(R.id.chat_username)
            dmButton.setOnClickListener { view ->
                run {
                    presenter.openDirectMessageChatRoom(contact.getUsername().toString())
                }
            }

         } else if (holder is ContactsHeaderViewHolder) {
            holder.bind(contactArrayList[position] as ContactsHeaderItemHolder)

        } else if (holder is InviteViewHolder) {
            holder.bind(contactArrayList[position] as InviteItemHolder)
            holder.itemView.setOnClickListener {
                shareApp()
            }
         } else if (holder is PermissionsViewHolder) {
            holder.bind(contactArrayList[position] as PermissionsItemHolder)
            holder.itemView.setOnClickListener {
                context.syncContacts(false, true)
            }
        }
    }

    private fun shareApp() {
        presenter.shareViaApp(context)
    }

    companion object {
        const val VIEW_TYPE_CONTACT = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_INVITE = 4
        const val VIEW_TYPE_PERMISSIONS = 6
    }
}
