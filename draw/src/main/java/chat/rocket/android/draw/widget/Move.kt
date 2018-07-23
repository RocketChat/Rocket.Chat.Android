package chat.rocket.android.draw.widget

import android.graphics.Path

class Move(val x: Float, val y: Float) : Action {

    override fun perform(path: Path) {
        path.moveTo(x, y)
    }
}