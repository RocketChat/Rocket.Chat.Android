package chat.rocket.android.chatroom.adapter

import android.content.Intent
import android.net.Uri
import android.view.View
import chat.rocket.android.chatroom.viewmodel.UrlPreviewViewModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.setVisible
import kotlinx.android.synthetic.main.message_url_preview.view.*

class UrlPreviewViewHolder(itemView: View) : BaseViewHolder<UrlPreviewViewModel>(itemView) {
    override fun bindViews(data: UrlPreviewViewModel) {
        with(itemView) {
            if (data.thumbUrl.isNullOrEmpty()) {
                image_preview.setVisible(false)
            } else {
                image_preview.setImageURI(data.thumbUrl)
                image_preview.setVisible(true)
            }
            text_host.content = data.hostname
            text_title.content = data.title
            text_description.content = data.description ?: ""

            url_preview_layout.setOnClickListener { view ->
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.rawData.url)))
            }
        }
    }

}