package chat.rocket.android.servers.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.util.extensions.inflate

private const val VIEW_TYPE_SERVER = 0
private const val VIEW_TYPE_ADD_NEW_SERVER = 1

class ServersAdapter(
    private val servers: List<Account>,
    private val currentServerUrl: String,
    private val selector: Selector
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SERVER -> ServerViewHolder(
                parent.inflate(R.layout.item_server), currentServerUrl
            )
            else -> AddNewServerViewHolder(parent.inflate(R.layout.item_add_new_server))
        }
    }

    override fun getItemCount() = servers.size + 1

    override fun getItemViewType(position: Int): Int {
        return when {
            position < servers.size -> VIEW_TYPE_SERVER
            else -> VIEW_TYPE_ADD_NEW_SERVER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ServerViewHolder -> bindServerViewHolder(holder, position)
            is AddNewServerViewHolder -> bindAddNewServerViewHolder(holder)
        }
    }

    private fun bindServerViewHolder(holder: ServerViewHolder, position: Int) {
        val account = servers[position]
        holder.bind(account)
        holder.itemView.setOnClickListener { selector.onServerSelected(account.serverUrl) }
    }

    private fun bindAddNewServerViewHolder(holder: AddNewServerViewHolder) {
        holder.itemView.setOnClickListener { selector.onAddNewServerSelected() }
    }
}

interface Selector {
    fun onServerSelected(serverUrl: String)
    fun onAddNewServerSelected()
}