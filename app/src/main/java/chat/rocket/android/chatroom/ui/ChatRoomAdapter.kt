package chat.rocket.android.chatroom.ui

import DateTimeHelper
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import chat.rocket.common.util.ifNull
import chat.rocket.core.model.Message
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.item_message.view.*

class ChatRoomAdapter(private val context: Context,
                      private var dataSet: MutableList<Message>,
                      private val serverUrl: String) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_message))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    fun addDataSet(dataSet: List<Message>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(previousDataSetSize, dataSet)
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) = with(itemView) {
            bindUserAvatar(message, image_user_avatar, image_unknown_user)
            bindUserName(message, text_user_name)
            bindTime(message, text_message_time)
            bindContent(message, text_content)
        }

        private fun bindUserAvatar(message: Message, drawee: SimpleDraweeView, imageUnknownUser: ImageView) = message.sender?.username.let {
            drawee.setImageURI(UrlHelper.getAvatarUrl(serverUrl, it.toString()))
        }.ifNull {
            imageUnknownUser.setVisibility(true)
        }

        private fun bindUserName(message: Message, textView: TextView) = message.sender?.username.let {
            textView.textContent = it.toString()
        }.ifNull {
            textView.textContent = context.getString(R.string.msg_unknown)
        }

        private fun bindTime(message: Message, textView: TextView) {
            textView.textContent = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(message.timestamp))
        }

        private fun bindContent(message: Message, textView: TextView) {
            textView.textContent = message.message
        }
    }
}