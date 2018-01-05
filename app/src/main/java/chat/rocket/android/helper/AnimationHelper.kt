package chat.rocket.android.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View

object AnimationHelper {

    /**
     * Shakes a view.
     */
    fun shakeView(viewToShake: View, x: Float = 2F, num: Int = 0) {
        if (num == 6) {
            viewToShake.translationX = 0.toFloat()
            return
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(ObjectAnimator.ofFloat(viewToShake, "translationX", dp(viewToShake.context, x)))
        animatorSet.duration = 50
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                shakeView(viewToShake, if (num == 5) 0.toFloat() else -x, num + 1)
            }
        })
        animatorSet.start()
    }

    /**
     * Vibrates the smart phone.
     */
    fun vibrateSmartPhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    private fun dp(context: Context, value: Float): Float {
        val density = context.resources.displayMetrics.density
        val result = Math.ceil(density.times(value.toDouble()))
        return result.toFloat()
    }
}