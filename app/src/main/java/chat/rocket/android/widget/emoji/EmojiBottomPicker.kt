package chat.rocket.android.widget.emoji

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import chat.rocket.android.R


class EmojiBottomPicker : DialogFragment() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    companion object {
        const val PREF_EMOJI_RECENTS = "PREF_EMOJI_RECENTS"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.emoji_popup_layout, container, false)
        viewPager = view.findViewById(R.id.pager_categories)
        tabLayout = view.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val parent = dialog.findViewById<View>(R.id.design_bottom_sheet)
                parent?.let {
                    val bottomSheetBehavior = BottomSheetBehavior.from(parent)
                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            }

                            override fun onStateChanged(bottomSheet: View, newState: Int) {
                                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                }
                            }
                        })
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                    }
                }
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val callback = when (activity) {
            is OnEmojiClickCallback -> activity as OnEmojiClickCallback
            else -> {
                val fragments = activity?.supportFragmentManager?.fragments
                if (fragments == null || fragments.size == 0 || !(fragments[0] is OnEmojiClickCallback)) {
                    throw IllegalStateException("activity/fragment should implement OnEmojiClickCallback interface")
                }
                fragments[0] as OnEmojiClickCallback
            }
        }

        viewPager.adapter = CategoryPagerAdapter(object : OnEmojiClickCallback {
            override fun onEmojiAdded(emoji: Emoji) {
                dismiss()
                EmojiLoader.addToRecents(emoji)
                callback.onEmojiAdded(emoji)
            }
        })

        for (category in EmojiCategory.values()) {
            val tab = tabLayout.getTabAt(category.ordinal)
            val tabView = layoutInflater.inflate(R.layout.emoji_picker_tab, null)
            tab?.setCustomView(tabView)
            val textView = tabView.findViewById(R.id.text) as TextView
            textView.text = category.icon()
        }

        viewPager.setCurrentItem(EmojiCategory.PEOPLE.ordinal)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!, theme)
    }

    interface OnEmojiClickCallback {
        fun onEmojiAdded(emoji: Emoji)
    }
}