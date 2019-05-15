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
    var permalink: String

    enum class ViewType(val viewType: Int) {
        MESSAGE(0),
        SYSTEM_MESSAGE(1),
        URL_PREVIEW(2),
        ATTACHMENT(3),
        MESSAGE_REPLY(4)
    }
}

internal fun Int.toViewType(): BaseUiModel.ViewType {
    return BaseUiModel.ViewType.values().firstOrNull { it.viewType == this }
        ?: throw InvalidParameterException("Invalid viewType: $this for BaseUiModel.ViewType")
}