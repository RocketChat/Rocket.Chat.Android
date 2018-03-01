package chat.rocket.android.util.extensions

import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator

fun View.rotateBy(value: Float, duration: Long = 100) {
    animate()
        .rotationBy(value)
        .setDuration(duration)
        .start()
}

fun View.fadeIn(start: Float = 0f, end: Float = 1f, duration: Long = 200) {
    // already at end alpha, just set visible and return
    if (alpha == end) {
        setVisible(true)
        return
    }

    val animation = animate()
            .alpha(end)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
    if (start != alpha) {
        animate()
                .alpha(start)
                .setDuration(duration / 2) // half the time, so the entire animation runs on duration
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    animation.setDuration(duration / 2).start()
                }.start()
    } else {
        animation.start()
    }
    setVisible(true)
}

fun View.fadeOut(start: Float = 1f, end: Float = 0f, duration: Long = 200) {
    if (alpha == end) {
        setVisible(false)
        return
    }

    val animation = animate()
            .alpha(end)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                setVisible(false)
            }

    if (start != alpha) {
        animate()
                .alpha(start)
                .setDuration(duration / 2) // half the time, so the entire animation runs on duration
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    animation.setDuration(duration / 2).start()
                }.start()
    } else {
        animation.start()
    }
}


fun View.circularRevealOrUnreveal(centerX: Int, centerY: Int, startRadius: Float, endRadius: Float, duration: Long = 200) {
    val anim = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
    anim.duration = duration

    if (startRadius < endRadius) {
        setVisible(true)
    } else {
        setVisible(false)
    }

    anim.start()
}