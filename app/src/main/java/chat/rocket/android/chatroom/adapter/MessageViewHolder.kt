package chat.rocket.android.chatroom.adapter

import android.text.method.LinkMovementMethod
import android.view.View
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageViewHolder(
    itemView: View,
    listener: ActionsListener
) : BaseViewHolder<MessageViewModel>(itemView, listener) {

    init {
        with(itemView) {
            text_content.movementMethod = LinkMovementMethod()
            setupActionMenu(text_content)
        }
    }

    override fun bindViews(data: MessageViewModel) {
        with(itemView) {
           if(data.showDayMarker){
                day.text = data.currentDayMarkerText
                day_marker_layout.visibility = View.VISIBLE
            } else day_marker_layout.visibility = View.GONE

            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
            image_avatar.setImageURI(data.avatar)
        }
    }
}