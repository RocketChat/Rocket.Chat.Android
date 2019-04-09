package chat.rocket.android.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.*
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.contacts.presentation.ContactsPresenter
import chat.rocket.android.contacts.ui.ContactsFragment
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.common.model.UserPresence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactsRecyclerViewAdapter(
        private val frag: ContactsFragment,
        private val presenter: ContactsPresenter,
        private val contactArrayList: List<ItemHolder<*>>
) : RecyclerView.Adapter<ViewHolder<*>>() {

    var contactsSelectionTracker: SelectionTracker<Long>? = null

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
            VIEW_TYPE_ACTION -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.item_contact_action, parent, false)
                ContactsActionViewHolder(binding.root)
            }
            VIEW_TYPE_PERMISSIONS -> {
                val view = parent.inflate(R.layout.item_permissions)
                PermissionsViewHolder(view)
            }
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite, Action or Permissions")
        }
    }

    override fun getItemCount() = contactArrayList.size

    override fun getItemId(position: Int): Long {
        val item = contactArrayList[position]
        return when (item) {
            is ContactsItemHolder -> position.toLong()
            is ContactsHeaderItemHolder -> position.toLong()
            is InviteItemHolder -> position.toLong()
            is ContactsActionItemHolder -> position.toLong()
            is PermissionsItemHolder -> position.toLong()
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite, Action or Permissions")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (contactArrayList[position]) {
            is ContactsItemHolder -> VIEW_TYPE_CONTACT
            is ContactsHeaderItemHolder -> VIEW_TYPE_HEADER
            is InviteItemHolder -> VIEW_TYPE_INVITE
            is ContactsActionItemHolder -> VIEW_TYPE_ACTION
            is PermissionsItemHolder -> VIEW_TYPE_PERMISSIONS
            else -> throw IllegalStateException("View type must be either Contact, Header, Invite, Action or Permissions")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        if (holder is ContactsViewHolder) {
            holder.bind(contactArrayList[position] as ContactsItemHolder)
            val contact: Contact = holder.data!!.data
            val userId = contact.getUserId()
            if (userId != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    val userPresence: UserPresence? = presenter.getUserPresence(userId)
                    if (userPresence != null) {
                        contact.setStatus(userPresence.presence!!)
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        holder.setContactStatus(contact)
                    }
                }
                contactsSelectionTracker?.let {
                    holder.bindSelection(it.isSelected(getItemId(position)))
                }
                if (!frag.enableGroups) {
                    // Clicking the row will open the DM
                    holder.itemView.setOnClickListener {
                        presenter.openDirectMessageChatRoom(contact.getUsername().toString())
                    }
                    // Clicking the @username button will open a DM
                    val dmButton: Button = holder.itemView.findViewById(R.id.chat_username)
                    dmButton.setOnClickListener { view ->
                        run {
                            presenter.openDirectMessageChatRoom(contact.getUsername().toString())
                        }
                    }
                }
            } else {
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
                // Clear any previous onClickListener when scrolling
                holder.itemView.setClickable(false)
            }
        } else if (holder is ContactsHeaderViewHolder) {
            holder.bind(contactArrayList[position] as ContactsHeaderItemHolder)
        } else if (holder is InviteViewHolder) {
            holder.bind(contactArrayList[position] as InviteItemHolder)
            holder.itemView.setOnClickListener {
                shareApp()
            }
        } else if (holder is ContactsActionViewHolder) {
            holder.bind(contactArrayList[position] as ContactsActionItemHolder)
        } else if (holder is PermissionsViewHolder) {
            holder.bind(contactArrayList[position] as PermissionsItemHolder)
            holder.itemView.setOnClickListener {
                (frag.activity as MainActivity).syncContacts(false, true)
            }
        }
    }

    private fun shareApp() {
        presenter.shareViaApp(frag.context!!)
    }

    companion object {
        const val VIEW_TYPE_CONTACT = 1
        const val VIEW_TYPE_HEADER = 2
        const val VIEW_TYPE_INVITE = 4
        const val VIEW_TYPE_ACTION = 5
        const val VIEW_TYPE_PERMISSIONS = 6
    }
}
