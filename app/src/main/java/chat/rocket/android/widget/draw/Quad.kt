package chat.rocket.android.widget.draw

import android.graphics.Path
import java.io.Writer

class Quad : Action {

    val x1: Float
    val y1: Float
    val x2: Float
    val y2: Float

    constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    override fun perform(path: Path) {
        path.quadTo(x1, y1, x2, y2)
    }

    override fun perform(writer: Writer) {
        writer.write("Q$x1,$y1 $x2,$y2")
    }
}
