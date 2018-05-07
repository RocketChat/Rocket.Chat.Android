package chat.rocket.android.chatroom.ui.bottomsheet.adapter

import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.util.SparseIntArray
import android.view.MenuItem
import chat.rocket.android.R
import ru.whalemare.sheetmenu.adapter.MenuAdapter

/**
 * A regular bottomsheet adapter with added possibility to hide or show a menu item given its item id.
 * Also added the possibility to change text colors for the menu items.
 */
open class ListBottomSheetAdapter(menuItems: List<MenuItem> = emptyList(), callback: MenuItem.OnMenuItemClickListener) :
        MenuAdapter(menuItems = menuItems, callback = callback, itemLayoutId = R.layout.item_linear, showIcons = true) {

    // Maps menu item ids to colors.
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
        menuItems.firstOrNull { it.itemId == itemId }?.apply {
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
        menuItems.firstOrNull { it.itemId == itemId }?.apply {
            setVisible(true)
            setEnabled(true)
        }
    }

    /**
     * Change a menu item text color given by its id to the given color.
     *
     * @param itemId The id of menu item.
     * @param color The color (not the resource color id) of the menu item.
     */
    fun setMenuItemTextColor(@IdRes itemId: Int, @ColorInt color: Int) {
        val itemIndex = menuItems.indexOfFirst { it.itemId == itemId }
        if (itemIndex > -1) {
            textColors.put(itemId, color)
        }
    }
}