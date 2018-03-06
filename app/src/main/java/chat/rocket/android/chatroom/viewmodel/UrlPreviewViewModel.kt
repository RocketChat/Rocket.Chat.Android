package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.R
import chat.rocket.core.model.url.Url

data class UrlPreviewViewModel(
    override val rawData: Url,
    override val messageId: String,
    val title: CharSequence?,
    val hostname: String,
    val description: CharSequence?,
    val thumbUrl: String?
) : BaseViewModel<Url> {
    override val viewType: Int
        get() = BaseViewModel.ViewType.URL_PREVIEW.viewType
    override val layoutId: Int
        get() = R.layout.message_url_preview
}