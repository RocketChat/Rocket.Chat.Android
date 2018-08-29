package chat.rocket.android.chatroom.uimodel

import chat.rocket.core.model.Message
import java.security.InvalidParameterException

interface BaseUiModel<out T> {
    val message: Message
    val rawData: T
    val messageId: String
    val viewType: Int
    val layoutId: Int
    var reactions: List<ReactionUiModel>
    var nextDownStreamMessage: BaseUiModel<*>?
    var preview: Message?
    var isTemporary: Boolean
    var unread: Boolean?
    var currentDayMarkerText: String
    var showDayMarker: Boolean
    var menuItemsToHide: MutableList<Int>

    enum class ViewType(val viewType: Int) {
        MESSAGE(0),
        SYSTEM_MESSAGE(1),
        URL_PREVIEW(2),
        IMAGE_ATTACHMENT(3),
        VIDEO_ATTACHMENT(4),
        AUDIO_ATTACHMENT(5),
        MESSAGE_ATTACHMENT(6),
        AUTHOR_ATTACHMENT(7),
        COLOR_ATTACHMENT(8),
        GENERIC_FILE_ATTACHMENT(9),
        MESSAGE_REPLY(10),
        ACTIONS_ATTACHMENT(11)
    }
}

internal fun Int.toViewType(): BaseUiModel.ViewType {
    return BaseUiModel.ViewType.values().firstOrNull { it.viewType == this }
            ?: throw InvalidParameterException("Invalid viewType: $this for BaseUiModel.ViewType")
}