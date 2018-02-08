package chat.rocket.android.chatroom.ui.bottomsheet

import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.adapter.MenuAdapter

class BottomSheetMenu(adapter: MenuAdapter) : SheetMenu(adapter = adapter) {

    override fun processRecycler(recycler: RecyclerView, dialog: BottomSheetDialog) {
        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(recycler.context, LinearLayoutManager.VERTICAL, false)
        }

        // Superclass SheetMenu adapter property is nullable MenuAdapter? but this class enforces
        // passing one at the constructor, so we assume it's always non-null.
        val adapter = adapter!!
        val callback = adapter.callback
        adapter.callback = MenuItem.OnMenuItemClickListener {
            callback?.onMenuItemClick(it)
            dialog.cancel()
            true
        }

        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
    }
}