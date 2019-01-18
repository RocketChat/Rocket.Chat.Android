package chat.rocket.android.members.adapter

import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.members.presentation.MembersPresenter
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extensions.inflate


class MembersAdapter(private val listener: (MemberUiModel) -> Unit, presenter: MembersPresenter?) :
    RecyclerView.Adapter<ViewHolder>() {
    private var dataSet: List<MemberUiModel> = ArrayList()
    private val enableActions: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_member), actionsListener)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(dataSet[position], listener)

    override fun getItemCount(): Int = dataSet.size

    fun clearData() {
        dataSet = emptyList()
        notifyDataSetChanged()
    }

    fun prependData(dataSet: List<MemberUiModel>) {
        this.dataSet = dataSet
        notifyItemRangeInserted(0, dataSet.size)
    }

    fun appendData(dataSet: List<MemberUiModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

<<<<<<< HEAD
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(memberUiModel: MemberUiModel, listener: (MemberUiModel) -> Unit) = with(itemView) {
            image_avatar.setImageURI(memberUiModel.avatarUri)
            text_member.content = memberUiModel.displayName
            text_member.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    DrawableHelper.getUserStatusDrawable(memberUiModel.status, context), null, null, null)
            setOnClickListener { listener(memberUiModel) }
=======
    private val actionsListener = object : ViewHolder.ActionsListener {
        override fun isActionsEnabled(): Boolean = enableActions
        override fun onActionSelected(item: MenuItem, member: MemberUiModel) {
            member.apply {
                when (item.itemId) {
                    R.id.action_member_set_owner-> {
                        presenter?.toggleOwner(this.userId, this.roles?.contains("owner") == true )
                    }
                    R.id.action_member_set_leader-> {
                        presenter?.toggleLeader(this.userId, this.roles?.contains("leader") == true)
                    }
                    R.id.action_member_set_moderator-> {
                        presenter?.toggleModerator(this.userId, this.roles?.contains("moderator") == true)
                    }
                    R.id.action_member_ignore-> {
                        presenter?.toggleIgnore(this.userId, false)
                    }
                    R.id.action_member_mute-> {
                        presenter?.toggleMute(this.username, this.muted)
                    }
                    R.id.action_member_remove-> {
                        presenter?.removeUser(this.userId)
                    }
                    else -> TODO("Not implemented")
                }
            }
>>>>>>> e494c1b5... add bottomSheet for members
        }
    }
}
