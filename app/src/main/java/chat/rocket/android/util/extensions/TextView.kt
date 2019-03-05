package chat.rocket.android.util.extensions

import android.content.Context
import android.os.Build
import android.widget.TextView

fun TextView.setTextViewAppearance(context: Context, style: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(style)
    } else {
        setTextAppearance(context, style)
    }
}