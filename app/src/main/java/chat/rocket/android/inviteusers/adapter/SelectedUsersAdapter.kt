package chat.rocket.android.inviteusers.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.members.uimodel.MemberUiModel
import com.facebook.drawee.view.SimpleDraweeView

class SelectedUsersAdapter(private val list: ArrayList<MemberUiModel>,
						   private val showRemoveButton: Boolean,
						   private val removeClickListener: (MemberUiModel) -> Unit)
	: RecyclerView.Adapter<SelectedUserViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedUserViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return SelectedUserViewHolder(inflater, parent)
	}

	override fun onBindViewHolder(holder: SelectedUserViewHolder, position: Int) {
		val member: MemberUiModel = list[position]
		holder.bind(member, showRemoveButton, removeClickListener)
	}

	override fun getItemCount(): Int = list.size
}

class SelectedUserViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
	RecyclerView.ViewHolder(inflater.inflate(R.layout.item_selected_contact, parent, false)) {
	private var name: TextView? = null
	private var avatar: SimpleDraweeView? = null
	private var removeButton: ImageView? = null

	init {
		name = itemView.findViewById(R.id.selected_contact_name)
		avatar = itemView.findViewById(R.id.selected_contact_image_avatar)
		removeButton = itemView.findViewById(R.id.remove_selected_contact)
	}

	fun bind(member: MemberUiModel, showRemoveButton: Boolean, removeClickListener: (MemberUiModel) -> Unit) {
		name?.text = member.username
		avatar?.setImageURI(member.avatarUri)
		if (showRemoveButton) {
			removeButton?.isVisible = true
			removeButton?.setOnClickListener {
				removeClickListener(member)
			}
			avatar?.setOnClickListener {
				removeClickListener(member)
			}
		}
	}
}