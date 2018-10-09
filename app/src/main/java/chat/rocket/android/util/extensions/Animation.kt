package chat.rocket.android.util.extensions

import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible

fun View.rotateBy(value: Float, duration: Long = 100) {
    animate()
        .rotationBy(value)
        .setDuration(duration)
        .start()
}

fun View.fadeIn(startValue: Float = 0f, finishValue: Float = 1f, duration: Long = 200) {
    if (alpha == finishValue) {
        isVisible = true
        return
    }

    animate()
        .alpha(startValue)
        .setDuration(duration / 2)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            animate()
                .alpha(finishValue)
                .setDuration(duration / 2)
                .setInterpolator(AccelerateInterpolator())
                .start()
        }.start()

    isVisible = true
}

fun View.fadeOut(startValue: Float = 1f, finishValue: Float = 0f, duration: Long = 200) {
    if (alpha == finishValue) {
        isVisible = false
        return
    }

    animate()
        .alpha(startValue)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction {
            animate()
                .alpha(finishValue)
                .setDuration(duration)
                .setInterpolator(AccelerateInterpolator())
                .start()
        }.start()

    isVisible = false
}

fun View.circularRevealOrUnreveal(
    centerX: Int,
    centerY: Int,
    startRadius: Float,
    endRadius: Float,
    duration: Long = 200
) {
    val anim =
        ViewAnimationUtils.createCircularReveal(this, centerX, centerY, startRadius, endRadius)
    anim.duration = duration

    isVisible = startRadius < endRadius

    anim.start()
}
