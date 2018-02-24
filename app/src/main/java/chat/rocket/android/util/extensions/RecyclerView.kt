package chat.rocket.android.util.extensions

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.scrollToBottom(visibleItems: Int = 40) {
    val manager = layoutManager
    if (manager != null && manager is LinearLayoutManager) {
        if (manager.findFirstVisibleItemPosition() > visibleItems) {
            scrollToPosition(0)
        } else {
            smoothScrollToPosition(0)
        }
    } else {
        scrollToPosition(0)
    }
}