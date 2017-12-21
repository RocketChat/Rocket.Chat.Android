package chat.rocket.android.core.behaviours

import android.view.View

interface ShakeView {

    /**
     * Shakes a view to indicate that it does not have the expected/correct value.
     *
     * @param viewToShake The view to shake.
     */
    fun shakeView(viewToShake: View)
}