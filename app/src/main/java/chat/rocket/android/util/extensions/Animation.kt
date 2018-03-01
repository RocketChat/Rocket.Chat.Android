package chat.rocket.android.util.extensions

import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

fun View.rotateBy(value: Float, duration: Long = 100) {
    animate()
        .rotationBy(value)
        .setDuration(duration)
        .start()
}

fun View.fadeIn(startValue: Float = 0f, finishValue: Float = 1f, duration: Long = 200) {
    if (alpha == finishValue) {
        setVisible(true)
        return
    }

    animate()
        .alpha(startValue)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction({
            animate()
                .alpha(finishValue)
                .setDuration(duration)
                .setInterpolator(AccelerateInterpolator()).start()
        }).start()

    setVisible(true)
}

fun View.fadeOut(startValue: Float = 1f, finishValue: Float = 0f, duration: Long = 200) {
    if (alpha == finishValue) {
        setVisible(false)
        return
    }

    animate()
        .alpha(startValue)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction({
            animate()
                .alpha(finishValue)
                .setDuration(duration)
                .setInterpolator(AccelerateInterpolator()).start()
        }).start()

    setVisible(false)
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