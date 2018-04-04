package chat.rocket.android.util.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.Fragment
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
        .setDuration(duration / 2)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction({
            animate()
                .alpha(finishValue)
                .setDuration(duration / 2)
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

fun View.shake(x: Float = 2F, num: Int = 0){
    if (num == 6) {
        this.translationX = 0.toFloat()
        return
    }

    val animatorSet = AnimatorSet()
    animatorSet.playTogether(ObjectAnimator.ofFloat(this, "translationX", this.context.dp(x)))
    animatorSet.duration = 50
    animatorSet.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            shake(if (num == 5) 0.toFloat() else -x, num + 1)
        }
    })
    animatorSet.start()
}

fun Context.dp(value: Float): Float {
    val density = this.resources.displayMetrics.density
    val result = Math.ceil(density.times(value.toDouble()))
    return result.toFloat()
}

fun Fragment.vibrateSmartPhone() {
    val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }

}