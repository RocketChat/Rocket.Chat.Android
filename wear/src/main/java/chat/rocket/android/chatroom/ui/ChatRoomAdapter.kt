package chat.rocket.android.chatroom.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.util.UserHelper
import kotlinx.android.synthetic.main.item_message.view.*
import javax.inject.Inject

class ChatRoomAdapter(
    private val fragmentContext: Context,
    data: List<MessageUiModel>,
    private val listener: (MessageUiModel) -> Unit
) :
    RecyclerView.Adapter<ChatRoomAdapter.MessageViewHolder>() {

    @Inject
    lateinit var userHelper: UserHelper
    private val dataItem: List<MessageUiModel>

    init {
        dataItem = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder =
        MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_message,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return dataItem.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bind(dataItem[position])

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(messageItem: MessageUiModel) {
            with(itemView) {
                if (userHelper.username() == messageItem.senderName) {
                    root_view.setBackgroundColor(resources.getColor(R.color.colorMessageBackgroundLight))
                    message_sender_name.text =
                            fragmentContext.getString(R.string.current_user_helper)
                } else {
                    message_sender_name.text = messageItem.senderName
                }
                message_time.text = messageItem.time
                message.text = messageItem.content
                message_sender_avatar.setImageURI(messageItem.avatar)
                if (messageItem.attachments) {
                    attachments_message.isVisible = true
                }
                setOnClickListener {
                    listener(messageItem)
                }
            }
        }
    }
}