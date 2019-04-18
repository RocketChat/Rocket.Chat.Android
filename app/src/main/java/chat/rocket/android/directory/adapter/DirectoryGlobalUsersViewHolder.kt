package chat.rocket.android.directory.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.directory.uimodel.DirectoryUiModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_directory_user.view.*

class DirectoryGlobalUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(directoryChannelUiModel: DirectoryUiModel) = with(itemView) {
        Glide.with(image_avatar).load(directoryChannelUiModel.userAvatarUri).into(image_avatar)
        text_user_name.text = directoryChannelUiModel.name
        text_user_username.text = directoryChannelUiModel.username
        with(text_server_url) {
            text = directoryChannelUiModel.serverUrl
            isVisible = true
        }
    }
}