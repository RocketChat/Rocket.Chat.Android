package chat.rocket.android.util.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import chat.rocket.android.core.GlideApp
import chat.rocket.android.core.GlideRequest

inline fun ImageView.setImageURI(url: String?) {
    GlideApp.with(this).load(url).into(this)
}

fun ImageView.setImageURI(url: String?, block: GlideRequest<Drawable>.() -> GlideRequest<Drawable>) {
    GlideApp.with(this).load(url).apply {
        block()
    }.into(this)
}