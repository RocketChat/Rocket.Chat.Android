package chat.rocket.android.draw.widget

import android.graphics.Path
import java.io.Serializable

interface Action : Serializable {

    fun perform(path: Path)
}