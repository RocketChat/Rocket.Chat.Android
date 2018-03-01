package chat.rocket.android.members.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.members.viewmodel.MemberViewModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_member.view.*

class MembersAdapter(private val listener: (MemberViewModel) -> Unit) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {
    private var dataSet: List<MemberViewModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersAdapter.ViewHolder = ViewHolder(parent.inflate(R.layout.item_member))

    override fun onBindViewHolder(holder: MembersAdapter.ViewHolder, position: Int) = holder.bind(dataSet[position], listener)

    override fun getItemCount(): Int = dataSet.size

    fun prependData(dataSet: List<MemberViewModel>) {
        this.dataSet = dataSet
        notifyItemRangeInserted(0, dataSet.size)
    }

    fun appendData(dataSet: List<MemberViewModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(memberViewModel: MemberViewModel, listener: (MemberViewModel) -> Unit) = with(itemView) {
            image_avatar.setImageURI(memberViewModel.avatarUri)
            text_member.content = memberViewModel.displayName

            setOnClickListener { listener(memberViewModel) }
        }
    }
}