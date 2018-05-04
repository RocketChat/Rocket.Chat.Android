package chat.rocket.android.widget.pageIndicator

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import chat.rocket.android.R

class PageIndicator : View, ViewPager.OnPageChangeListener {

    private var mViewPager: ViewPager? = null
    private var activePaint: Paint? = null
    private var inActivePaint: Paint? = null

    private var circlePadding: Int = 0
    private var circleRadius: Int = 0
    private var circleCount: Int = 0

    private var mGravity: Int = 0
    private var mState: Int = 0
    private var mPageOffSet: Float = 0.toFloat()
    private var mCurrentDragPage: Int = 0
    private var mSelectedPage: Int = 0
    private var currentNormalOffset: Float = 0.toFloat()
    private var currentRelativePageOffset: Float = 0.toFloat()
    private var startedSettleNormalOffset: Float = 0.toFloat()
    private var startedSettlePageOffset: Float = 0.toFloat()

    private val desiredHeight: Int
        get() = paddingTop + paddingBottom + circleRadius * 2

    private val desiredWidth: Int
        get() = paddingLeft + paddingRight + circleRadius * 2 * circleCount + (circleCount - 1) * circlePadding

    val startedX: Int
        get() {
            when (mGravity) {
                Gravity.LEFT, GravityCompat.START -> return paddingLeft
                Gravity.RIGHT, GravityCompat.END -> return measuredWidth - paddingRight - allCircleWidth
                Gravity.CENTER -> return measuredWidth / 2 - allCircleWidth / 2
                else -> return measuredWidth / 2 - allCircleWidth / 2
            }
        }

    val allCircleWidth: Int
        get() = circleRadius * 2 * circleCount + (circleCount - 1) * circlePadding

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (isInEditMode) {
            circleCount = 3
        }
        circleRadius = resources.getDimension(R.dimen.default_circle_radius).toInt()
        circlePadding = resources.getDimension(R.dimen.default_circle_padding).toInt()
        var inactiveColor = ContextCompat.getColor(context, R.color.pageIndicatorInactive)
        var activeColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val gravity = Gravity.CENTER

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.PageIndicator, 0, 0)
            circleRadius = typedArray.getDimension(R.styleable.PageIndicator_indicatorRadius, circleRadius.toFloat()).toInt()
            circlePadding = typedArray.getDimension(R.styleable.PageIndicator_indicatorPadding, circlePadding.toFloat()).toInt()
            inactiveColor = typedArray.getColor(R.styleable.PageIndicator_indicatorInActiveColor, inactiveColor)
            activeColor = typedArray.getColor(R.styleable.PageIndicator_indicatorActiveColor, activeColor)
            mGravity = typedArray.getInt(R.styleable.PageIndicator_android_gravity, gravity)
        }

        activePaint = Paint()
        activePaint!!.color = activeColor
        inActivePaint = Paint()
        inActivePaint!!.color = inactiveColor
    }

    fun initViewPager(viewPager: ViewPager) {
        if (mViewPager === viewPager)
            return

        if (mViewPager != null)
            viewPager.addOnPageChangeListener(this)

        if (viewPager.adapter == null)
            throw IllegalStateException("ViewPager doesn't have an adapter isntance.")

        mViewPager = viewPager
        mViewPager!!.addOnPageChangeListener(this)
        circleCount = viewPager.adapter!!.count
        mCurrentDragPage = viewPager.currentItem
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = View.resolveSize(desiredHeight, heightMeasureSpec)
        val width = View.resolveSize(desiredWidth, widthMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        // draw behind circles
        for (i in 0 until circleCount) {
            val center = getCircleCenter(i)
            canvas.drawCircle(center, (paddingTop + circleRadius).toFloat(), circleRadius.toFloat(), inActivePaint!!)
        }
        drawRect(canvas)
    }

    private fun drawRect(canvas: Canvas) {
        if (mViewPager == null || mViewPager!!.adapter == null || mViewPager!!.adapter!!.count == 0)
            return

        val top = paddingTop.toFloat()
        val bottom = top + circleRadius * 2
        val moveDistance = (circleRadius * 2 + circlePadding).toFloat()
        val isDragForward = mSelectedPage - mCurrentDragPage < 1

        val relativePageOffset = if (isDragForward) mPageOffSet else 1.0f - mPageOffSet
        currentRelativePageOffset = relativePageOffset

        val shiftedOffset = relativePageOffset * OFFSET_MULTIPLIER_NORMAL

        val settleShiftedOffset = Math.max(0f, mapValue(relativePageOffset, startedSettlePageOffset, 1.0f, startedSettleNormalOffset, 1.0f))

        val normalOffset = if (mState == ViewPager.SCROLL_STATE_SETTLING) settleShiftedOffset else shiftedOffset
        currentNormalOffset = normalOffset

        val largerOffset = Math.min(if (mState == ViewPager.SCROLL_STATE_SETTLING) relativePageOffset * OFFSET_MULTIPLIER_SETTLING else relativePageOffset * OFFSET_MULTIPLIER_DRAG, 1.0f)

        val circleCenter = getCircleCenter(if (isDragForward) mCurrentDragPage else mSelectedPage)

        val normal = moveDistance * normalOffset
        val large = moveDistance * largerOffset

        val left = if (isDragForward) circleCenter - circleRadius + normal else circleCenter - circleRadius.toFloat() - large
        val right = if (isDragForward) circleCenter + circleRadius.toFloat() + large else circleCenter + circleRadius - normal

        val rectF = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectF, circleRadius.toFloat(), circleRadius.toFloat(), activePaint!!)
    }

    private fun getCircleCenter(position: Int): Float {
        return startedX.toFloat() + circleRadius.toFloat() + getCirclePadding(position)
    }

    private fun getCirclePadding(position: Int): Float {
        return (circlePadding * position + circleRadius * 2 * position).toFloat()
    }

    private fun mapValue(value: Float, a1: Float, a2: Float, b1: Float, b2: Float): Float {
        return b1 + (value - a1) * (b2 - b1) / (a2 - a1)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        mCurrentDragPage = position
        mPageOffSet = positionOffset
        postInvalidate()
    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {
        mState = state
        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_DRAGGING) {
            mSelectedPage = mViewPager!!.currentItem
            currentNormalOffset = 0f
            currentRelativePageOffset = 0f
        } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
            startedSettleNormalOffset = currentNormalOffset
            startedSettlePageOffset = currentRelativePageOffset
        }
    }

    override fun onDetachedFromWindow() {
        if (mViewPager != null)
            mViewPager!!.removeOnPageChangeListener(this)
        //finish modified
        super.onDetachedFromWindow()
    }

    companion object {
        private const val OFFSET_MULTIPLIER_DRAG = 1.2f
        private const val OFFSET_MULTIPLIER_SETTLING = 1.4f
        private const val OFFSET_MULTIPLIER_NORMAL = 0.30f
    }
}