package chat.rocket.android.chatroom.ui.bottomsheet.adapter

import android.graphics.Color
import android.view.MenuItem
import android.view.View
import chat.rocket.android.R

/**
 * An adapter for bottomsheet menu that lists all the actions that could be taken over a chat message.
 */
class ActionListAdapter(menuItems: List<MenuItem> = emptyList(), callback: MenuItem.OnMenuItemClickListener) :
        ListBottomSheetAdapter(menuItems = menuItems, callback = callback) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = menuItems[position]

        if (showIcons) {
            if (item.icon == null) {
                holder.imageIcon.visibility = View.GONE
            } else {
                holder.imageIcon.visibility = View.VISIBLE
            }
        } else {
            holder.imageIcon.visibility = View.GONE
        }

        holder.imageIcon.setImageDrawable(item.icon)
        holder.textTitle.text = item.title
        holder.itemView.setOnClickListener {
            callback?.onMenuItemClick(item)
        }
        val color = if (item.itemId == R.id.action_menu_msg_delete) Color.RED else textColors.get(item.itemId)
        holder.textTitle.setTextColor(color)
    }
}