package chat.rocket.android.directory.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.directory.uimodel.DirectoryUiModel
import chat.rocket.android.util.extensions.inflate

private const val VIEW_TYPE_CHANNELS = 0
private const val VIEW_TYPE_USERS = 1
private const val VIEW_TYPE_GLOBAL_USERS = 2

class DirectoryAdapter(private val selector: Selector) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isSortByChannels: Boolean = true
    private var isSearchForGlobalUsers: Boolean = true
    private var dataSet: List<DirectoryUiModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_CHANNELS -> DirectoryChannelViewHolder(
                parent.inflate(R.layout.item_directory_channel)
            )
            VIEW_TYPE_USERS -> DirectoryUsersViewHolder(
                parent.inflate(R.layout.item_directory_user)
            )
            VIEW_TYPE_GLOBAL_USERS -> DirectoryGlobalUsersViewHolder(
                parent.inflate(R.layout.item_directory_user)
            )
            else -> throw IllegalStateException("viewType must be either VIEW_TYPE_CHANNELS, VIEW_TYPE_USERS or VIEW_TYPE_GLOBAL_USERS")
        }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (isSortByChannels) {
            VIEW_TYPE_CHANNELS
        } else {
            if (isSearchForGlobalUsers) {
                VIEW_TYPE_GLOBAL_USERS
            } else {
                VIEW_TYPE_USERS
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is DirectoryChannelViewHolder -> bindDirectoryChannelViewHolder(holder, position)
            is DirectoryUsersViewHolder -> bindDirectoryUsersViewHolder(holder, position)
            is DirectoryGlobalUsersViewHolder -> bindDirectoryGlobalUsersViewHolder(
                holder,
                position
            )
            else -> throw IllegalStateException("Unable to bind ViewHolder. ViewHolder must be either DirectoryChannelViewHolder, DirectoryUsersViewHolder or DirectoryGlobalUsersViewHolder")
        }

    private fun bindDirectoryChannelViewHolder(holder: DirectoryChannelViewHolder, position: Int) {
        with(dataSet[position]) {
            holder.bind(this)
            holder.itemView.setOnClickListener { selector.onChannelSelected(id, name) }
        }
    }

    private fun bindDirectoryUsersViewHolder(holder: DirectoryUsersViewHolder, position: Int) {
        with(dataSet[position]) {
            holder.bind(this)
            holder.itemView.setOnClickListener { selector.onUserSelected(username, name) }
        }
    }

    private fun bindDirectoryGlobalUsersViewHolder(
        holder: DirectoryGlobalUsersViewHolder,
        position: Int
    ) {
        with(dataSet[position]) {
            holder.bind(this)
            holder.itemView.setOnClickListener { selector.onGlobalUserSelected(username, name) }
        }
    }

    fun clearData() {
        dataSet = emptyList()
        notifyDataSetChanged()
    }

    fun setSorting(isSortByChannels: Boolean, isSearchForGlobalUsers: Boolean) {
        this.isSortByChannels = isSortByChannels
        this.isSearchForGlobalUsers = isSearchForGlobalUsers
    }

    fun prependData(dataSet: List<DirectoryUiModel>) {
        this.dataSet = dataSet
        notifyItemRangeInserted(0, dataSet.size)
    }

    fun appendData(dataSet: List<DirectoryUiModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }
}

interface Selector {
    fun onChannelSelected(channelId: String, channelName: String)
    fun onUserSelected(username: String, name: String)
    fun onGlobalUserSelected(username: String, name: String)
}