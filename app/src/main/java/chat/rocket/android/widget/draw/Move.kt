package chat.rocket.android.widget.draw

import android.graphics.Path
import java.io.Writer
import java.security.InvalidParameterException

class Move : Action {

    val x: Float
    val y: Float

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun perform(path: Path) {
        path.moveTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("M$x,$y")
    }
}
