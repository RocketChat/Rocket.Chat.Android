package chat.rocket.android.draw.widget

import android.graphics.Path

class Quad(private val x1: Float, private val y1: Float, private val x2: Float, private val y2: Float) : Action {

    override fun perform(path: Path) {
        path.quadTo(x1, y1, x2, y2)
    }
}