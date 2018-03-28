package chat.rocket.android.chatroom.adapter

import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.view.View
import chat.rocket.android.chatroom.viewmodel.UrlPreviewViewModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_url_preview.view.*

class UrlPreviewViewHolder(itemView: View,
                           listener: ActionsListener,
                           reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<UrlPreviewViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(url_preview_layout)
        }
    }

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

            val tabsbuilder = CustomTabsIntent.Builder()
            val customTabsIntent = tabsbuilder.build()

            url_preview_layout.setOnClickListener { view ->
                customTabsIntent.launchUrl(context, Uri.parse(data.rawData.url.toLowerCase()))
            }
        }
    }

}