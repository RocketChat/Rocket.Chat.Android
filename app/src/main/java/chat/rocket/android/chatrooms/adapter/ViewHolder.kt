package chat.rocket.android.chatrooms.adapter

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.bottomsheet.ChatRoomActionBottomSheet
import chat.rocket.android.chatroom.ui.bottomsheet.MessageActionsBottomSheet
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.util.extensions.ifNotNullNorEmpty
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.toList
import chat.rocket.core.model.isSystemMessage

abstract class ViewHolder<T : ItemHolder<*>>(
    itemView: View,
    private val listener: ActionsListener
) : RecyclerView.ViewHolder(itemView),
    MenuItem.OnMenuItemClickListener {
    var data: T? = null

    init {
        setupActionMenu(itemView)
    }

    fun bind(data: T) {
        this.data = data
        bindViews(data)
    }

    abstract fun bindViews(data: T)

    interface ActionsListener {
        fun isActionsEnabled(): Boolean
        fun onActionSelected(item: MenuItem, room: RoomUiModel)
    }


    internal fun setupActionMenu(view: View) {
        view.setOnLongClickListener{
            if (data?.data is RoomUiModel) {
            data?.let { vm ->
              vm.data.let {
                val menuItems = view.context.inflate(R.menu.chatrooms_action).toList()
                menuItems.find { it.itemId == R.id.action_favorite_room }?.apply {
                  setTitle(if ((it as RoomUiModel).favorite == true) R.string.action_unfavorite else R.string.action_favorite)
                  setIcon(if (it.favorite == true) R.drawable.ic_star_black_24dp else R.drawable.ic_star_border_black_24dp)
                  isChecked = (it.favorite==true)
                }
                menuItems.find { it.itemId == R.id.action_read }?.apply {
                  setTitle(if ((it as RoomUiModel).unread.isNullOrEmpty()) R.string.action_mark_unread else R.string.action_mark_read)
                }
                view.context?.let {
                  if (it is ContextThemeWrapper && it is AppCompatActivity) {
                    with(it) {
                      val actionsBottomSheet = ChatRoomActionBottomSheet()
                      actionsBottomSheet.addItems(menuItems, this@ViewHolder)
                      actionsBottomSheet.show(supportFragmentManager, null)
                    }
                  }
                }
              }
            }
            }
            true
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        data?.let {
            listener.onActionSelected(item, (it as RoomItemHolder).data)
        }
        return true
    }
}