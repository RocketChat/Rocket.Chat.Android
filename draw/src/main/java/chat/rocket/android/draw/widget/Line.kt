package chat.rocket.android.draw.widget

import android.graphics.Path

class Line(val x: Float, val y: Float) : Action {

    override fun perform(path: Path) {
        path.lineTo(x, y)
    }
}