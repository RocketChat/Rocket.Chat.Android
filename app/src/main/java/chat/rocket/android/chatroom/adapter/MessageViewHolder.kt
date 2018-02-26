package chat.rocket.android.chatroom.adapter

import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.ui.bottomsheet.BottomSheetMenu
import chat.rocket.android.chatroom.ui.bottomsheet.adapter.ActionListAdapter
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import ru.whalemare.sheetmenu.extension.inflate
import ru.whalemare.sheetmenu.extension.toList

class MessageViewHolder(
    itemView: View,
    private val roomType: String,
    private val roomName: String,
    private val presenter: ChatRoomPresenter?,
    enableActions: Boolean
) : BaseViewHolder<MessageViewModel>(itemView),
    MenuItem.OnMenuItemClickListener {

    init {
        itemView.text_content.movementMethod = LinkMovementMethod()

        if (enableActions) {
            itemView.setOnLongClickListener {
                if (data?.isSystemMessage == false) {
                    val menuItems = it.context.inflate(R.menu.message_actions).toList()
                    menuItems.find { it.itemId == R.id.action_menu_msg_pin_unpin }?.apply {
                        val isPinned = data?.isPinned ?: false
                        setTitle(if (isPinned) R.string.action_msg_unpin else R.string.action_msg_pin)
                        isChecked = isPinned
                    }
                    val adapter = ActionListAdapter(menuItems, this@MessageViewHolder)
                    BottomSheetMenu(adapter).apply {

                    }.show(it.context)
                }
                true
            }
        }
    }

    override fun bindViews(data: MessageViewModel) {
        with(itemView) {
            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
            image_avatar.setImageURI(data.avatar)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        data?.rawData?.apply {
            when (item.itemId) {
                R.id.action_menu_msg_delete -> presenter?.deleteMessage(roomId, id)
                R.id.action_menu_msg_quote -> presenter?.citeMessage(roomType, roomName, id, false)
                R.id.action_menu_msg_reply -> presenter?.citeMessage(roomType, roomName, id, true)
                R.id.action_menu_msg_copy -> presenter?.copyMessage(id)
                R.id.action_menu_msg_edit -> presenter?.editMessage(roomId, id, message)
                R.id.action_menu_msg_pin_unpin -> {
                    with(item) {
                        if (!isChecked) {
                            presenter?.pinMessage(id)
                        } else {
                            presenter?.unpinMessage(id)
                        }
                    }
                }
                else -> TODO("Not implemented")
            }
        }
        return true
    }
}