package chat.rocket.android.util.extensions

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.scrollToBottom() {
    val manager = layoutManager
    if (manager != null && manager is LinearLayoutManager) {
        if (manager.findFirstVisibleItemPosition() > 40) {
            scrollToPosition(0)
        } else {
            smoothScrollToPosition(0)
        }
    } else {
        scrollToPosition(0)
    }
}