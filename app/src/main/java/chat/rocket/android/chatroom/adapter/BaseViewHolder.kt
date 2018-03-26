package chat.rocket.android.chatroom.adapter

import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.bottomsheet.BottomSheetMenu
import chat.rocket.android.chatroom.ui.bottomsheet.adapter.ActionListAdapter
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.widget.emoji.Emoji
import chat.rocket.android.widget.emoji.EmojiReactionListener
import chat.rocket.core.model.Message
import chat.rocket.core.model.isSystemMessage
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import ru.whalemare.sheetmenu.extension.inflate
import ru.whalemare.sheetmenu.extension.toList


abstract class BaseViewHolder<T : BaseViewModel<*>>(
        itemView: View,
        private val listener: ActionsListener,
        var reactionListener: EmojiReactionListener? = null
) : RecyclerView.ViewHolder(itemView),
        MenuItem.OnMenuItemClickListener {
    var data: T? = null

    init {
        setupActionMenu(itemView)
    }

    fun bind(data: T) {
        this.data = data
        bindViews(data)
        bindReactions()
    }

    private fun bindReactions() {
        data?.let {
            val recyclerView = itemView.findViewById(R.id.recycler_view_reactions) as RecyclerView
            val adapter: MessageReactionsAdapter
            if (recyclerView.adapter == null) {
                adapter = MessageReactionsAdapter()
            } else {
                adapter = recyclerView.adapter as MessageReactionsAdapter
                adapter.clear()
            }

            if (it.nextDownStreamMessage == null) {
                adapter.listener = object : EmojiReactionListener {
                    override fun onReactionTouched(messageId: String, emojiShortname: String) {
                        reactionListener?.onReactionTouched(messageId, emojiShortname)
                    }

                    override fun onReactionAdded(messageId: String, emoji: Emoji) {
                        if (!adapter.contains(emoji.shortname)) {
                            reactionListener?.onReactionAdded(messageId, emoji)
                        }
                    }
                }
                val context = itemView.context
                val manager = FlexboxLayoutManager(context, FlexDirection.ROW)
                recyclerView.layoutManager = manager
                recyclerView.adapter = adapter
                adapter.addReactions(it.reactions.filterNot { it.unicode.startsWith(":") })
            }
        }
    }

    abstract fun bindViews(data: T)

    interface ActionsListener {
        fun isActionsEnabled(): Boolean
        fun onActionSelected(item: MenuItem, message: Message)
    }

    val longClickListener = { view: View ->
        if (data?.message?.isSystemMessage() == false) {
            val menuItems = view.context.inflate(R.menu.message_actions).toList()
            menuItems.find { it.itemId == R.id.action_menu_msg_pin_unpin }?.apply {
                val isPinned = data?.message?.pinned ?: false
                setTitle(if (isPinned) R.string.action_msg_unpin else R.string.action_msg_pin)
                isChecked = isPinned
            }
            val adapter = ActionListAdapter(menuItems, this@BaseViewHolder)
            BottomSheetMenu(adapter).show(view.context)
        }
        true
    }

    internal fun setupActionMenu(view: View) {
        if (listener.isActionsEnabled()) {
            view.setOnLongClickListener(longClickListener)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        data?.let {
            listener.onActionSelected(item, it.message)
        }
        return true
    }
}