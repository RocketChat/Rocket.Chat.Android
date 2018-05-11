package chat.rocket.android.util.extensions

import chat.rocket.core.model.Message
import chat.rocket.core.model.isSystemMessage

fun Message.isBroadcastReplyAvailable(isBroadcastChannel: Boolean): Boolean {
    return (isTemporary == false) && !isSystemMessage() && isBroadcastChannel
}