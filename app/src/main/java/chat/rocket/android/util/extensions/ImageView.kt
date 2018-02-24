package chat.rocket.android.util.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import chat.rocket.android.core.GlideApp
import chat.rocket.android.core.GlideRequest

fun ImageView.setImageURI(url: String?, block: GlideRequest<Drawable>.() -> Unit = { this }) {
    GlideApp.with(this).load(url).apply {
        this.block()
        this
    }.into(this)
}