package chat.rocket.android.main.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.util.extensions.inflate

class AccountsAdapter(
    private val accounts: List<Account>,
    private val selector: AccountSelector
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ACCOUNT -> AccountViewHolder(parent.inflate(R.layout.item_account))
            else -> AddAccountViewHolder(parent.inflate(R.layout.item_add_account))
        }
    }

    override fun getItemCount() = accounts.size + 1

    override fun getItemViewType(position: Int) =
            if (position == accounts.size) VIEW_TYPE_ADD_ACCOUNT else VIEW_TYPE_ACCOUNT

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AccountViewHolder -> bindAccountViewHolder(holder, position)
            is AddAccountViewHolder -> bindAddAccountViewHolder(holder, position)
        }
    }

    private fun bindAccountViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.bind(account)
        holder.itemView.setOnClickListener {
            selector.onAccountSelected(account.serverUrl)
        }
    }

    private fun bindAddAccountViewHolder(holder: AddAccountViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            selector.onAddedAccountSelected()
        }
    }
}

interface AccountSelector {
    fun onAccountSelected(serverUrl: String)
    fun onAddedAccountSelected()
}

private const val VIEW_TYPE_ACCOUNT = 0
private const val VIEW_TYPE_ADD_ACCOUNT = 1