package chat.rocket.android.widget.emoji

import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.util.extensions.setVisible


class EmojiFragment : Fragment() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var editor: View
    internal lateinit var parentContainer: ViewGroup
    var softKeyboardVisible = false

    companion object {
        const val PREF_EMOJI_RECENTS = "PREF_EMOJI_RECENTS"
        const val PREF_KEYBOARD_HEIGHT = "PREF_KEYBOARD_HEIGHT"
        val TAG: String = EmojiFragment::class.java.simpleName
        fun newInstance(editor: View) = EmojiFragment().apply { this.editor = editor }

        fun getOrAttach(activity: FragmentActivity, @IdRes containerId: Int, editor: View): EmojiFragment {
            val fragmentManager = activity.supportFragmentManager
            var fragment: Fragment? = fragmentManager.findFragmentByTag(TAG)
            return if (fragment == null) {
                fragment = newInstance(editor)
                fragment.parentContainer = activity.findViewById(containerId)
                fragmentManager.beginTransaction()
                        .replace(containerId, fragment, TAG)
                        .commit()
                fragment
            } else {
                fragment as EmojiFragment
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.emoji_popup_layout, container, false)
        parentContainer = view.findViewById(R.id.emoji_keyboard_container)
        viewPager = view.findViewById(R.id.pager_categories)
        tabLayout = view.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
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

        activity?.let {
            val decorView = it.getWindow().decorView
            decorView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                private val windowVisibleDisplayFrame = Rect()
                private var lastVisibleDecorViewHeight: Int = 0

                override fun onGlobalLayout() {
                    // Retrieve visible rectangle inside window.
                    decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
                    val visibleDecorViewHeight = windowVisibleDisplayFrame.height()

                    // Decide whether keyboard is visible from changing decor view height.
                    if (lastVisibleDecorViewHeight != 0) {
                        if (lastVisibleDecorViewHeight > visibleDecorViewHeight + 150) {
                            // Calculate current keyboard height (this includes also navigation bar height when in fullscreen mode).
                            val currentKeyboardHeight = decorView.height - windowVisibleDisplayFrame.bottom - editor.measuredHeight
                            // Notify listener about keyboard being shown.
                            EmojiRepository.saveKeyboardHeight(currentKeyboardHeight)
                            setKeyboardHeight(currentKeyboardHeight)
                            softKeyboardVisible = true
                            show()
                        } else if (lastVisibleDecorViewHeight + 150 < visibleDecorViewHeight) {
                            // Notify listener about keyboard being hidden.
                            softKeyboardVisible = false
                        }
                    }
                    // Save current decor view height for the next call.
                    lastVisibleDecorViewHeight = visibleDecorViewHeight
                }
            })
        }

        val storedHeight = EmojiRepository.getKeyboardHeight()
        if (storedHeight > 0) {
            setKeyboardHeight(storedHeight)
        }

        viewPager.adapter = CategoryPagerAdapter(object : OnEmojiClickCallback {
            override fun onEmojiAdded(emoji: Emoji) {
                EmojiRepository.addToRecents(emoji)
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

        val currentTab = if (EmojiRepository.getRecents().isEmpty()) EmojiCategory.PEOPLE.ordinal else
            EmojiCategory.RECENTS.ordinal
        viewPager.setCurrentItem(currentTab)
    }

    private fun setKeyboardHeight(height: Int) {
        parentContainer.layoutParams.height = height
        parentContainer.requestLayout()
    }

    class EmojiTextWatcher(val editor: EditText) : TextWatcher {
        @Volatile private var emojiToRemove = mutableListOf<EmojiTypefaceSpan>()

        override fun afterTextChanged(s: Editable) {
            val message = editor.getEditableText()

            // Commit the emoticons to be removed.
            for (span in emojiToRemove.toList()) {
                val start = message.getSpanStart(span)
                val end = message.getSpanEnd(span)

                // Remove the span
                message.removeSpan(span)

                // Remove the remaining emoticon text.
                if (start != end) {
                    message.delete(start, end)
                }
                break
            }
            emojiToRemove.clear()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (after < count) {
                val end = start + count
                val message = editor.getEditableText()
                val list = message.getSpans(start, end, EmojiTypefaceSpan::class.java)

                for (span in list) {
                    val spanStart = message.getSpanStart(span)
                    val spanEnd = message.getSpanEnd(span)
                    if (spanStart < end && spanEnd > start) {
                        // Add to remove list
                        emojiToRemove.add(span)
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }
    }

    fun show() {
        parentContainer.setVisible(true)
    }

    fun hide() {
        parentContainer.setVisible(false)
    }

    fun isShown() = parentContainer.visibility == View.VISIBLE

    interface OnEmojiClickCallback {
        /**
         * Callback triggered after an emoji is selected on the picker.
         *
         * @param emoji The selected emoji
         */
        fun onEmojiAdded(emoji: Emoji)
    }
}