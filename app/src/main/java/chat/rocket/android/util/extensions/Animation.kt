package chat.rocket.android.util.extensions

import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

fun View.rotateBy(value: Float, duration: Long = 200) {
    animate()
        .rotationBy(value)
        .setDuration(duration)
        .start()
}

fun View.fadeIn(startValue: Float, finishValue: Float, duration: Long = 200) {
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

fun View.fadeOut(startValue: Float, finishValue: Float, duration: Long = 200) {
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

fun View.circularRevealOrUnreveal(centerX: Int, centerY: Int, startRadius: Float, endRadius: Float, duration: Long = 600) {
    val anim = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
    anim.duration = duration

    if (startRadius < endRadius) {
        setVisible(true)
    } else {
        setVisible(false)
    }

    anim.start()
}