package chat.rocket.android.chatroom.ui

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseIntArray
import android.view.MenuItem
import android.view.View
import chat.rocket.android.R
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.adapter.MenuAdapter

open class ListBottomSheetAdapter(menuItems: List<MenuItem> = emptyList(), callback: MenuItem.OnMenuItemClickListener) :
        MenuAdapter(menuItems = menuItems, callback = callback, itemLayoutId = R.layout.item_linear, showIcons = true) {

    protected val textColors: SparseIntArray = SparseIntArray(menuItems.size)

    init {
        for (item in menuItems) {
            textColors.put(item.itemId, Color.BLACK)
        }
    }

    /**
     * Hide a menu item and disable it.
     *
     * @param itemId The id of the menu item to disable and hide.
     */
    fun hideMenuItem(@IdRes itemId: Int) {
        val item = menuItems.firstOrNull { it.itemId == itemId }
        item?.apply {
            setVisible(false)
            setEnabled(false)
        }
    }

    /**
     * Show a menu item and enable it.
     *
     * @param itemId The id of the menu item to enable and show.
     */
    fun showMenuItem(@IdRes itemId: Int) {
        val item = menuItems.firstOrNull { it.itemId == itemId }
        item?.apply {
            setVisible(true)
            setEnabled(true)
        }
    }

    fun setMenuItemTextColor(@IdRes itemId: Int, @ColorInt color: Int) {
        val itemIndex = menuItems.indexOfFirst { it.itemId == itemId }
        if (itemIndex > -1) {
            textColors.put(itemId, color)
        }
    }
}

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

class BottomSheetMenu(adapter: MenuAdapter) : SheetMenu(adapter = adapter) {

    override fun processRecycler(recycler: RecyclerView, dialog: BottomSheetDialog) {
        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(recycler.context, LinearLayoutManager.VERTICAL, false)
        }

        val callback = adapter?.callback
        adapter?.callback = MenuItem.OnMenuItemClickListener {
            callback?.onMenuItemClick(it)
            dialog.cancel()
            true
        }

        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
    }
}



