package chat.rocket.android.widget.draw

import android.graphics.Path
import java.io.Writer

class Quad(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : Action {

    override fun perform(path: Path) {
        path.quadTo(x1, y1, x2, y2)
    }

    override fun perform(writer: Writer) {
        writer.write("Q$x1,$y1 $x2,$y2")
    }
}
