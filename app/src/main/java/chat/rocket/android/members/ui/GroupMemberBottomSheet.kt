package chat.rocket.android.members.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.message_action_item.view.*
import kotlinx.android.synthetic.main.message_bottomsheet.*

class GroupMemberBottomSheet: BottomSheetDialogFragment() {

    private val adapter = GroupMemberActionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.message_bottomsheet, container, false)
    }

    fun addItems(items: List<MenuItem>, itemClickListener: MenuItem.OnMenuItemClickListener) {
        adapter.addItems(items, ActionItemClickListener(dismissAction = { dismiss() },
                itemClickListener = itemClickListener))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bottomsheet_recycler_view.layoutManager = LinearLayoutManager(context)
        bottomsheet_recycler_view.adapter = adapter
    }

    private class ActionItemClickListener(
            val dismissAction: () -> Unit,
            val itemClickListener: MenuItem.OnMenuItemClickListener
    )

    private class GroupMemberActionAdapter : RecyclerView.Adapter<GroupMemberActionViewHolder>() {

        private lateinit var itemClickListener: ActionItemClickListener
        private val menuItems = mutableListOf<MenuItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberActionViewHolder {
            return GroupMemberActionViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.message_action_item, parent, false)
            )
        }

        override fun getItemCount() = menuItems.size

        override fun onBindViewHolder(holder: GroupMemberActionViewHolder, position: Int) {
            holder.bind(menuItems[position], itemClickListener)
        }

        fun addItems(items: List<MenuItem>, itemClickListener: ActionItemClickListener) {
            this.itemClickListener = itemClickListener
            menuItems.clear()
            menuItems.addAll(items)
            notifyDataSetChanged()
        }
    }

    private class GroupMemberActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MenuItem, itemClickListener: ActionItemClickListener) {
            with(itemView) {
                message_action_title.text = item.title
                message_action_icon.setImageDrawable(item.icon)
                setOnClickListener {
                    itemClickListener.itemClickListener.onMenuItemClick(item)
                    itemClickListener.dismissAction.invoke()
                }
            }
        }
    }
}
