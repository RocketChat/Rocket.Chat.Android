package chat.rocket.android.chatrooms.ui

import android.view.View

fun View.animateFABMenuItem(translate: Float, alpha: Float, scale: Float) {
    this.animate()
            .translationY(translate)
            .alpha(alpha)
            .scaleY(scale)
}

fun View.animateFABMenuItem(translate: Float, alpha: Float, scale: Float, fabAnimatorListener: ChatRoomsFragment.FABAnimatorListener) {
    this.animate()
            .translationY(translate)
            .alpha(alpha)
            .scaleY(scale)
            .setListener(fabAnimatorListener)
}
