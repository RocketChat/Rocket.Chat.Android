package chat.rocket.android.util.extensions

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun ViewGroup.inflate(@LayoutRes resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}