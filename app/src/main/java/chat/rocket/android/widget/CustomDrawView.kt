package chat.rocket.android.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomDrawView: View {

    val strokeWidth = 4f
    private var mX: Float = 0f
    private var mY:Float = 0f
    private lateinit var path: Path
    private lateinit var paint: Paint
    private val TOLERANCE = 5f
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas

    fun init(){
        path = Path()
        paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = strokeWidth
        this.isDrawingCacheEnabled = true
    }
    constructor(context: Context?) : super(context){ init() }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){ init() }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init() }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawPath(path,paint)
    }

    private fun startTouch(x: Float,y: Float){
        path.moveTo(x,y)
        mX = x
        mY = y
    }

    private fun moveTouch(x: Float,y: Float){
        val dx: Float = Math.abs(x-mX)
        val dy: Float = Math.abs(y-mY)
        if (dx >= TOLERANCE || dy >= TOLERANCE){
            path.quadTo(mX,mY,(x+mX)/2,(y+mY)/2)
            mX = x
            mY = y
        }
    }

    private fun upTouch(){
        path.lineTo(mX,mY)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x: Float = event!!.x
        val y: Float = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                startTouch(x,y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                moveTouch(x,y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                upTouch()
                invalidate()
            }
        }
        return true
    }

    fun getBitmap(): Bitmap {
        return this.drawingCache
    }
}