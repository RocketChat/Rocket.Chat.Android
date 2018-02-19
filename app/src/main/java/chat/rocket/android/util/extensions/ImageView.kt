package chat.rocket.android.util.extensions

import android.widget.ImageView
import chat.rocket.android.core.GlideApp

inline fun ImageView.setImageURI(url: String) {
    GlideApp.with(this).load(url).into(this)
}