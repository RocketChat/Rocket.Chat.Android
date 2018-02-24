package chat.rocket.android.widget

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.graphics.withTranslation

class AvatarTextDrawable(private val text: String = "",
                         color: Int = Color.GRAY,
                         private val textSize: Int = -1,
                         radius: Float = 4f,
                         textColor: Int = Color.WHITE,
                         textBold: Boolean = false,
                         textFont: Typeface = Cache.get("sans-serif", Typeface.NORMAL)
) : ShapeDrawable() {

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var fontSized: Boolean = false

    init {
        paint.color = color

        textPaint.style = Paint.Style.FILL
        textPaint.color = textColor
        textPaint.isFakeBoldText = textBold
        textPaint.typeface = textFont
        textPaint.textAlign = Paint.Align.CENTER

        if (textSize > 0) {
            textPaint.textSize = textSize.toFloat()
            fontSized = true
        }

        if (radius > 0) {
            val radii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
            this.shape = RoundRectShape(radii, null, null)
        } else {
            this.shape = RectShape()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        canvas.withTranslation(r.left.toFloat(), r.top.toFloat()) {
            if (!fontSized) {
                val fontSize = if (textSize < 0) Math.min(r.width(), r.height()) / 2 else textSize
                textPaint.textSize = fontSize.toFloat()
                fontSized = true
            }

            canvas.drawText(text, (r.width() / 2).toFloat(),
                    r.height() / 2 - (textPaint.descent() + textPaint.ascent()) / 2,
                    textPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        textPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return -1
    }

    override fun getIntrinsicHeight(): Int {
        return -1
    }

    private object Cache {
        val cache = HashMap<String, Typeface>()

        fun get(typeface: String, style: Int = Typeface.NORMAL): Typeface {
            val key = "${typeface}_$style"
            val result = cache[key]
            if (result == null) {
                cache[key] = Typeface.create(typeface, style)
            }

            return result ?: Typeface.DEFAULT
        }
    }
}