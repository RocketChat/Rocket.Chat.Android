package chat.rocket.android.members.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_member.view.*

class MembersAdapter(
    private val listener: (MemberUiModel) -> Unit
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {
    private var dataSet: List<MemberUiModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_member))

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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(memberUiModel: MemberUiModel, listener: (MemberUiModel) -> Unit) = with(itemView) {
            image_avatar.setImageURI(memberUiModel.avatarUri)
            text_member.content = memberUiModel.displayName
            text_member.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    DrawableHelper.getUserStatusDrawable(memberUiModel.status, context), null, null, null)
            setOnClickListener { listener(memberUiModel) }
        }
    }
}
