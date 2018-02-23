package chat.rocket.android.util.avatar

import android.graphics.Color
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.regex.Pattern

class AvatarDecoder : ResourceDecoder<ByteBuffer, Avatar> {

    private val colorPattern = Pattern.compile("fill=\"(.*?)\"/>")
    private val letterPattern1 = Pattern.compile("\">\\n(.*?)\\n</text>")
    private val letterPattern2 = Pattern.compile("\">(.*?)</text>")

    override fun handles(source: ByteBuffer, options: Options): Boolean {
        try {
            val firstFourBytes = source.int

            source.rewind()

            if (firstFourBytes == SVG_HEADER || firstFourBytes == XML_HEADER) {
                return true
            }
            Timber.d("can't handle this signature: $firstFourBytes")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return false
    }

    override fun decode(source: ByteBuffer, width: Int, height: Int, options: Options): Resource<Avatar>? {
        try {
            Timber.d("Decoding SVG Avatar")
            val xmlBytes = if (source.hasArray()) {
                source.array().take(source.remaining()).toByteArray()
            } else {
                val tmp = ByteArray(source.remaining())
                source.get(tmp)
                tmp
            }

            val xml = String(xmlBytes)
            val color = findColor(xml)
            val text = findText(xml)

            return SimpleResource<Avatar>(Avatar(color, text ?: ""))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    private fun findText(xml: String): String? {
        val matcher1 = letterPattern1.matcher(xml)
        if (matcher1.find()) {
            return matcher1.group(1)
        }

        val matcher2 = letterPattern2.matcher(xml)
        if (matcher2.find()) {
            return matcher2.group(1)
        }

        return null
    }

    private fun findColor(xml: String): Int {
        val matcher = colorPattern.matcher(xml)
        if (matcher.find()) {
            val colorString = matcher.group(1)
            try {
                return Color.parseColor(colorString)
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }
        }
        matcher.end()

        return DEFAULT_COLOR
    }
}

private const val SVG_HEADER = 0x3C737667 // <svg
private const val XML_HEADER = 0x3C3F786D // <?xm
private const val DEFAULT_COLOR = 0xF44336

infix fun Byte.shl(shit: Int): Int = this.toInt() shl shit