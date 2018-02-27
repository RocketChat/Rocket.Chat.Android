package chat.rocket.android.widget.share.ui

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.widget.share.ui.ShareRoomAdapter.ShareRoomViewHolder
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_chat.view.*

class ShareRoomAdapter(private val settings: PublicSettings,
                       private val onSelect: (chatRoom: ChatRoom, content: Any?) -> Unit) : RecyclerView.Adapter<ShareRoomViewHolder>() {
    private val roomDataSet = arrayListOf<ChatRoom>()
    private var contentToShare: Any? = null

    override fun onBindViewHolder(holder: ShareRoomViewHolder, position: Int) {
        holder.bind(roomDataSet[position])
    }

    override fun getItemCount() = roomDataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareRoomViewHolder {
        return ShareRoomViewHolder(parent.inflate(R.layout.item_chat))
    }

    fun updateRooms(newRooms: List<ChatRoom>, contentToShare: Any?) {
        this.contentToShare = contentToShare
        roomDataSet.clear()
        roomDataSet.addAll(newRooms)
        notifyItemRangeInserted(0, roomDataSet.size)
    }

    inner class ShareRoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, image_avatar)
            bindName(chatRoom, text_chat_name)

            text_chat_name.setTextColor(ContextCompat.getColor(context,
                    R.color.colorSecondaryText))

            setOnClickListener {
                onSelect(chatRoom, contentToShare)
            }
        }

        private fun bindAvatar(chatRoom: ChatRoom, drawee: SimpleDraweeView) {
            val avatarId = if (chatRoom.type is RoomType.DirectMessage) chatRoom.name else "@${chatRoom.name}"
            drawee.setImageURI(UrlHelper.getAvatarUrl(chatRoom.client.url, avatarId))
        }

        private fun bindName(chatRoom: ChatRoom, textView: TextView) {
            if (chatRoom.type is RoomType.DirectMessage && settings.useRealName()) {
                textView.content = chatRoom.fullName ?: chatRoom.name
            } else {
                textView.content = chatRoom.name
            }
        }

    }
}