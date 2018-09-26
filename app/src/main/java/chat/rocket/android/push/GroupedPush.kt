package chat.rocket.android.push

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

typealias TupleGroupIdMessageCount = Pair<Int, AtomicInteger>

class GroupedPush {
    // Notifications received from the same server are isGrouped in a single bundled notification.
    // This map associates a host to a group id.
    val groupMap = HashMap<String, TupleGroupIdMessageCount>()

    // Map a hostname to a list of push messages that pertain to it.
    val hostToPushMessageList = HashMap<String, MutableList<PushMessage>>()
}
