package chat.rocket.android.widget.share.ui

import android.net.Uri
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
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.item_share.view.*
import java.util.concurrent.CopyOnWriteArrayList


class ShareRoomAdapter(private val settings: PublicSettings,
                       private val onSelect: (chatRoom: ChatRoom, content: Any?) -> Unit) : RecyclerView.Adapter<ShareRoomViewHolder>() {
    private val rooms = CopyOnWriteArrayList<ChatRoom>()
    private var contentToShare: Any? = null

    override fun onBindViewHolder(holder: ShareRoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount() = rooms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ShareRoomViewHolder(parent.inflate(R.layout.item_share), settings, contentToShare, onSelect)

    fun updateRooms(newRooms: List<ChatRoom>, contentToShare: Any?) {
        this.contentToShare = contentToShare
        val added = rooms.addAllAbsent(newRooms)
        notifyItemRangeInserted(rooms.size, added)
    }

    class ShareRoomViewHolder(view: View,
                              private val settings: PublicSettings,
                              private val contentToShare: Any?,
                              private val onSelect: (chatRoom: ChatRoom, content: Any?) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, itemView.findViewById(R.id.image_share_avatar))
            bindName(chatRoom, itemView.findViewById(R.id.text_share_name))

            text_share_name.setTextColor(ContextCompat.getColor(context,
                    R.color.colorSecondaryText))

            setOnClickListener {
                onSelect(chatRoom, contentToShare)
            }
        }

        private fun bindAvatar(chatRoom: ChatRoom, drawee: SimpleDraweeView) {
            val avatarId = if (chatRoom.type is RoomType.DirectMessage) chatRoom.name else "@${chatRoom.name}"
            val url = Uri.parse(UrlHelper.getAvatarUrl(chatRoom.client.url, avatarId))
            val request = ImageRequestBuilder.newBuilderWithSource(url)
                    .setProgressiveRenderingEnabled(false)
                    .build()
            val controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setAutoPlayAnimations(false)
                    .build()

            val roundingParams = RoundingParams()
            roundingParams.roundAsCircle = true
            drawee.hierarchy.roundingParams = roundingParams

            drawee.controller = controller
//            drawee.setImageURI(UrlHelper.getAvatarUrl(chatRoom.client.url, avatarId))
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