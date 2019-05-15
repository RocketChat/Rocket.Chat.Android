package chat.rocket.android.directory.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.directory.uimodel.DirectoryUiModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_directory_channel.view.*

class DirectoryChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(directoryChannelUiModel: DirectoryUiModel) = with(itemView) {
        Glide.with(image_avatar).load(directoryChannelUiModel.channelAvatarUri).into(image_avatar)
        text_channel_name.text = directoryChannelUiModel.name
        text_channel_description.text = directoryChannelUiModel.description
        text_channel_total_members.text = directoryChannelUiModel.totalMembers
    }
}