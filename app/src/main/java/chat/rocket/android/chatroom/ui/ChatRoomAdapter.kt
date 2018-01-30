package chat.rocket.android.chatroom.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import chat.rocket.common.util.ifNull
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class ChatRoomAdapter(private val serverUrl: String) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {
    private val dataSet = ArrayList<MessageViewModel>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_message), serverUrl)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    fun addDataSet(dataSet: List<MessageViewModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet.addAll(previousDataSetSize, dataSet)
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    fun addItem(message: MessageViewModel) {
        dataSet.add(0, message)
        notifyItemInserted(0)
    }

    fun updateItem(index: Int, message: MessageViewModel) {
        dataSet[index] = message
        notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.hashCode().toLong()
    }

    class ViewHolder(itemView: View, val serverUrl: String) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: MessageViewModel) = with(itemView) {
            bindUserAvatar(message, image_avatar, image_unknown_avatar)
            text_user_name.textContent = message.sender
            text_message_time.textContent = message.time
            text_content.text = message.content
        }

        private fun bindUserAvatar(message: MessageViewModel, drawee: SimpleDraweeView, imageUnknownAvatar: ImageView) = message.getAvatarUrl(serverUrl).let {
            drawee.setImageURI(it.toString())
        }.ifNull {
            imageUnknownAvatar.setVisibility(true)
        }
    }
}