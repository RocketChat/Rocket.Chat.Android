package chat.rocket.android.chatroom.ui

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.USE_REALNAME
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import chat.rocket.common.util.ifNull
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.Value
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class ChatRoomAdapter(private val context: Context,
                      private val serverUrl: String,
                      private val settings: Map<String, Value<Any>>?) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    val dataSet = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_message))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    fun addDataSet(dataSet: List<Message>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(previousDataSetSize, dataSet)
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    fun addItem(message: Message) {
        dataSet.add(0, message)
        notifyItemInserted(0)
    }

    fun updateItem(index: Int, message: Message) {
        dataSet[index] = message
        notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.hashCode().toLong()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) = with(itemView) {
            bindUserAvatar(message, image_avatar, image_unknown_avatar)
            bindUserName(message, text_user_name)
            bindTime(message, text_message_time)
            bindContent(message, text_content)
        }

        private fun bindUserAvatar(message: Message, drawee: SimpleDraweeView, imageUnknownAvatar: ImageView) = message.sender?.username.let {
            drawee.setImageURI(UrlHelper.getAvatarUrl(serverUrl, it.toString()))
        }.ifNull {
            imageUnknownAvatar.setVisibility(true)
        }

        private fun bindUserName(message: Message, textView: TextView) {
            val useRealName = settings?.get(USE_REALNAME)?.value as Boolean
            val username = message.sender?.username
            val realName = message.sender?.name
            val senderName = if (useRealName) realName else username
            senderName.let {
                // TODO: Fallback to username if real name happens to be null. ATM this could happen if the
                // present message is a system message. We should handle that on the SDK
                textView.textContent = if (senderName == null) username.toString() else it.toString()
            }.ifNull {
                textView.textContent = context.getString(R.string.msg_unknown)
            }

        }
        private fun bindTime(message: Message, textView: TextView) {
            textView.textContent = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(message.timestamp))
        }

        private fun bindContent(message: Message, textView: TextView) {
            when (message.type) {
                //TODO: Add implementation for other types.
                //TODO: Move all those strings to xml. Refer to https://github.com/RocketChat/Rocket.Chat.Android/blob/develop/app/src/main/res/values/system_message_strings.xml
                MessageType.MESSAGE_REMOVED -> setSystemMessage(message, textView,
                        "Message removed")
                MessageType.USER_JOINED ->  setSystemMessage(message, textView,
                        "Has joined the channel.")
                MessageType.USER_LEFT -> setSystemMessage(message, textView,
                        "Has left the channel.")
                MessageType.USER_ADDED -> setSystemMessage(message, textView,
                        "User ${message.message} added by ${message.sender?.username}")
                else -> textView.textContent = message.message
            }
        }

        private fun setSystemMessage(message: Message, textView: TextView, msg: String) {
            val spannableMsg = SpannableString(msg)
            spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length,
                    0)
            spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length,
                    0)

            if (message.type == MessageType.USER_ADDED) {
                val userAddedStartIndex = 5
                val userAddedLastIndex = userAddedStartIndex + message.message.length
                val addedByStartIndex = userAddedLastIndex + 10
                val addedByLastIndex = addedByStartIndex + message.sender?.username!!.length
                spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), userAddedStartIndex, userAddedLastIndex,
                        0)
                spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), addedByStartIndex, addedByLastIndex,
                        0)
            }
            textView.text = spannableMsg
        }
    }
}