package chat.rocket.android.emoji

import android.content.Context
import android.database.DataSetObserver
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class EmojiViewPager : ViewPager {

    private var mLayoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR
    private val mPageChangeListeners = hashMapOf<OnPageChangeListener, ReversingOnPageChangeListener>()

    private val isRtl: Boolean
        get() = mLayoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        val viewCompatLayoutDirection = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            ViewCompat.LAYOUT_DIRECTION_RTL
        } else {
            ViewCompat.LAYOUT_DIRECTION_LTR
        }
        if (viewCompatLayoutDirection != mLayoutDirection) {
            val adapter = super.getAdapter()
            var position = 0
            if (adapter != null) {
                position = currentItem
            }
            mLayoutDirection = viewCompatLayoutDirection
            if (adapter != null) {
                adapter.notifyDataSetChanged()
                currentItem = position
            }
        }
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        val adapter = if (adapter != null) {
            ReversingAdapter(adapter)
        } else {
            adapter
        }
        super.setAdapter(adapter)
        currentItem = 0
    }

    override fun getAdapter(): PagerAdapter? {
        return super.getAdapter() as ReversingAdapter?
    }

    override fun getCurrentItem(): Int {
        var item = super.getCurrentItem()
        val adapter = super.getAdapter()
        if (adapter != null && isRtl) {
            item = adapter.count - item - 1
        }
        return item
    }

    override fun setCurrentItem(position: Int, smoothScroll: Boolean) {
        val adapter = super.getAdapter()
        val position = if (adapter != null && isRtl) {
            adapter.count - position - 1
        } else {
            position
        }
        super.setCurrentItem(position, smoothScroll)
    }

    override fun setCurrentItem(position: Int) {
        val adapter = super.getAdapter()
        val position = if (adapter != null && isRtl) {
            adapter.count - position - 1
        } else {
            position
        }
        super.setCurrentItem(position)
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        super.setOnPageChangeListener(ReversingOnPageChangeListener(listener))
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        val reversingListener = ReversingOnPageChangeListener(listener)
        mPageChangeListeners.put(listener, reversingListener)
        super.addOnPageChangeListener(reversingListener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        val reverseListener = mPageChangeListeners.remove(listener)
        if (reverseListener != null) {
            super.removeOnPageChangeListener(reverseListener)
        }
    }

    override fun clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners()
        mPageChangeListeners.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpec = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            var height = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                val h = child.measuredHeight
                if (h > height) {
                    height = h
                }
            }
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private inner class ReversingOnPageChangeListener(
        private val mListener: OnPageChangeListener
    ) : OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            var position = position
            var positionOffset = positionOffset
            var positionOffsetPixels = positionOffsetPixels
            val width = width
            val adapter = super@EmojiViewPager.getAdapter()
            if (adapter != null && isRtl) {
                val count = adapter.count
                var remainingWidth = (width * (1 - adapter.getPageWidth(position))).toInt() +
                    positionOffsetPixels
                while (position < count && remainingWidth > 0) {
                    position += 1
                    remainingWidth -= (width * adapter.getPageWidth(position)).toInt()
                }
                position = count - position - 1
                positionOffsetPixels = -remainingWidth
                positionOffset = positionOffsetPixels / (width * adapter.getPageWidth(position))
            }
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            val adapter = super@EmojiViewPager.getAdapter()
            val position = if (adapter != null && isRtl) {
                adapter.count - position - 1
            } else {
                position
            }
            mListener.onPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            mListener.onPageScrollStateChanged(state)
        }
    }

    private inner class ReversingAdapter(private val adapter: PagerAdapter) : PagerAdapter() {

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return adapter.isViewFromObject(view, obj)
        }

        override fun getCount(): Int {
            return adapter.count
        }

        override fun getItemPosition(obj: Any): Int {
            var position = adapter.getItemPosition(obj)
            if (isRtl) {
                if (position == POSITION_UNCHANGED || position == POSITION_NONE) {
                    position = POSITION_NONE
                } else {
                    position = getCount() - position - 1
                }
            }
            return position
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return adapter.getPageTitle(position)
        }

        override fun getPageWidth(position: Int): Float {
            return adapter.getPageWidth(position)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            return adapter.instantiateItem(container, position)
        }

        override fun instantiateItem(container: View, position: Int): Any {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            return adapter.instantiateItem(container, position)
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            adapter.destroyItem(container, position, obj)
        }

        override fun destroyItem(container: View, position: Int, obj: Any) {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            adapter.destroyItem(container, position, obj)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            adapter.setPrimaryItem(container, position, obj)
        }

        override fun setPrimaryItem(container: View, position: Int, obj: Any) {
            val position = if (isRtl) {
                count - position - 1
            } else {
                position
            }
            adapter.setPrimaryItem(container, position, obj)
        }

        override fun startUpdate(container: ViewGroup) {
            adapter.startUpdate(container)
        }

        override fun startUpdate(container: View) {
            adapter.startUpdate(container)
        }

        override fun finishUpdate(container: ViewGroup) {
            adapter.finishUpdate(container)
        }

        override fun finishUpdate(container: View) {
            adapter.finishUpdate(container)
        }

        override fun saveState(): Parcelable? {
            return adapter.saveState()
        }

        override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
            adapter.restoreState(state, loader)
        }

        override fun notifyDataSetChanged() {
            adapter.notifyDataSetChanged()
        }

        override fun registerDataSetObserver(observer: DataSetObserver) {
            adapter.registerDataSetObserver(observer)
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            adapter.unregisterDataSetObserver(observer)
        }
    }
}
