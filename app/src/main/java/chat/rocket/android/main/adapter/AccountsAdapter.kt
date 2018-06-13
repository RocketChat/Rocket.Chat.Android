package chat.rocket.android.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.util.extensions.inflate
import chat.rocket.common.model.UserStatus

private const val VIEW_TYPE_CHANGE_STATUS = 0
private const val VIEW_TYPE_ACCOUNT = 1
private const val VIEW_TYPE_ADD_ACCOUNT = 2

class AccountsAdapter(
    private val accounts: List<Account>,
    private val selector: Selector
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHANGE_STATUS -> StatusViewHolder(parent.inflate(R.layout.item_change_status))
            VIEW_TYPE_ACCOUNT -> AccountViewHolder(parent.inflate(R.layout.item_account))
            else -> AddAccountViewHolder(parent.inflate(R.layout.item_add_account))
        }
    }

    override fun getItemCount() = accounts.size + 2

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_CHANGE_STATUS
            position <= accounts.size -> VIEW_TYPE_ACCOUNT
            else -> VIEW_TYPE_ADD_ACCOUNT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StatusViewHolder -> bindStatusViewHolder(holder)
            is AccountViewHolder -> bindAccountViewHolder(holder, position)
            is AddAccountViewHolder -> bindAddAccountViewHolder(holder)
        }
    }

    private fun bindStatusViewHolder(holder: StatusViewHolder) {
        holder.bind { userStatus -> selector.onStatusSelected(userStatus) }
    }

    private fun bindAccountViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position - 1]
        holder.bind(account)
        holder.itemView.setOnClickListener {
            selector.onAccountSelected(account.serverUrl)
        }
    }

    private fun bindAddAccountViewHolder(holder: AddAccountViewHolder) {
        holder.itemView.setOnClickListener {
            selector.onAddedAccountSelected()
        }
    }
}

interface Selector {
    fun onStatusSelected(userStatus: UserStatus)
    fun onAccountSelected(serverUrl: String)
    fun onAddedAccountSelected()
}