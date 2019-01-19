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
        holder.bind(dataSet[position], position, listener)

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
        override fun onActionSelected(item: MenuItem, member: MemberUiModel, index: Int) {
            member.apply {
                when (item.itemId) {
                    R.id.action_member_set_owner-> {
                        val isOwner = this.roles?.contains("owner") == true
                        presenter?.toggleOwner(this.userId, isOwner) {
                            if (isOwner)
                                dataSet[index].roles = dataSet[index].roles?.filterNot { it == "owner"}
                            else
                                dataSet[index].roles = dataSet[index].roles?.plus("owner")
                            notifyItemChanged(index)
                        }
                    }
                    R.id.action_member_set_leader-> {
                        val isLeader = this.roles?.contains("leader") == true
                        presenter?.toggleLeader(this.userId, isLeader) {
                        if (isLeader)
                            dataSet[index].roles = dataSet[index].roles?.filterNot { it == "leader"}
                        else
                            dataSet[index].roles = dataSet[index].roles?.plus("leader")
                        notifyItemChanged(index)
                        }
                    }
                    R.id.action_member_set_moderator-> {
                        val isMod = this.roles?.contains("moderator") == true
                        presenter?.toggleModerator(this.userId, isMod) {
                            if (isMod)
                                dataSet[index].roles = dataSet[index].roles?.filterNot { it == "moderator" }
                            else
                                dataSet[index].roles = dataSet[index].roles?.plus("moderator")
                            notifyItemChanged(index)
                        }
                    }
                    R.id.action_member_ignore-> {
                            TODO("not implemented")
//                        presenter?.toggleIgnore(this.userId, false){}
                    }
                    R.id.action_member_mute-> {
                        presenter?.toggleMute(this.username, this.muted) {
                            dataSet[index].muted = !this.muted
                            notifyItemChanged(index)
                        }
                    }
                    R.id.action_member_remove-> {
                        presenter?.removeUser(this.userId) {
                            dataSet = dataSet.filterIndexed{ position, _-> position != index }
                            notifyItemRemoved(index)
                            notifyItemRangeChanged(index, dataSet.size)
                        }
                    }
                    else -> TODO("Not implemented")
                }
            }
>>>>>>> e494c1b5... add bottomSheet for members
        }
    }
}
