package chat.rocket.android.chatroom.models.messages

import chat.rocket.core.model.Message
import java.security.InvalidParameterException

interface BaseUiModel<out T> {
    val message: Message
    val rawData: T
    val messageId: String
    val viewType: Int
    val layoutId: Int

    enum class ViewType(val viewType: Int) {
        MESSAGE(0)
        //enum created so that we can add other message types later on
    }
}

internal fun Int.toViewType(): BaseUiModel.ViewType {
    return BaseUiModel.ViewType.values().firstOrNull { it.viewType == this }
            ?: throw InvalidParameterException("Invalid viewType: $this for BaseUiModel.ViewType")
}