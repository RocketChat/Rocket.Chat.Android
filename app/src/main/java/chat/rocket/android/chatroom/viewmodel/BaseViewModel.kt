package chat.rocket.android.chatroom.viewmodel

import chat.rocket.core.model.Message
import java.security.InvalidParameterException

interface BaseViewModel<out T> {
    val message: Message
    val rawData: T
    val messageId: String
    val viewType: Int
    val layoutId: Int
    var reactions: List<ReactionViewModel>
    var nextDownStreamMessage: BaseViewModel<*>?
    var preview: Message?

    enum class ViewType(val viewType: Int) {
        MESSAGE(0),
        SYSTEM_MESSAGE(1),
        URL_PREVIEW(2),
        IMAGE_ATTACHMENT(3),
        VIDEO_ATTACHMENT(4),
        AUDIO_ATTACHMENT(5),
        MESSAGE_ATTACHMENT(6)
    }
}

internal fun Int.toViewType(): BaseViewModel.ViewType {
    return BaseViewModel.ViewType.values().firstOrNull { it.viewType == this }
            ?: throw InvalidParameterException("Invalid viewType: $this for BaseViewModel.ViewType")
}