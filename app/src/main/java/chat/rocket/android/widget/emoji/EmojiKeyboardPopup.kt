package chat.rocket.android.widget.emoji

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import chat.rocket.android.R


class EmojiKeyboardPopup(context: Context, view: View) : OverKeyboardPopupWindow(context, view) {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: View
    private lateinit var backspaceView: View
    private lateinit var parentContainer: ViewGroup
    var listener: Listener? = null

    companion object {
        const val PREF_EMOJI_RECENTS = "PREF_EMOJI_RECENTS"
    }

    override fun onCreateView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.emoji_popup_layout, null, false)
        parentContainer = view.findViewById(R.id.emoji_keyboard_container)
        viewPager = view.findViewById(R.id.pager_categories)
        searchView = view.findViewById(R.id.emoji_search)
        backspaceView = view.findViewById(R.id.emoji_backspace)
        tabLayout = view.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
        return view
    }

    override fun onViewCreated(view: View) {
        setupViewPager()
        setupBottomBar()
    }

    private fun setupBottomBar() {
        searchView.setOnClickListener {
        }

        backspaceView.setOnClickListener {
            listener?.onNonEmojiKeyPressed(KeyEvent.KEYCODE_BACK)
        }
    }

    private fun setupViewPager() {
        context.let {
            val callback = when (it) {
                is Listener -> it
                else -> {
                    val fragments = (it as AppCompatActivity).supportFragmentManager.fragments
                    if (fragments == null || fragments.size == 0 || !(fragments[0] is Listener)) {
                        throw IllegalStateException("activity/fragment should implement Listener interface")
                    }
                    fragments[0] as Listener
                }
            }
            viewPager.adapter = CategoryPagerAdapter(object : Listener {
                override fun onNonEmojiKeyPressed(keyCode: Int) {
                    // do nothing
                }

                override fun onEmojiAdded(emoji: Emoji) {
                    EmojiRepository.addToRecents(emoji)
                    callback.onEmojiAdded(emoji)
                }
            })

            for (category in EmojiCategory.values()) {
                val tab = tabLayout.getTabAt(category.ordinal)
                val tabView = LayoutInflater.from(context).inflate(R.layout.emoji_picker_tab, null)
                tab?.setCustomView(tabView)
                val textView = tabView.findViewById(R.id.image_category) as ImageView
                textView.setImageResource(category.resourceIcon())
            }

            val currentTab = if (EmojiRepository.getRecents().isEmpty()) EmojiCategory.PEOPLE.ordinal else
                EmojiCategory.RECENTS.ordinal
            viewPager.setCurrentItem(currentTab)
        }
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

    interface Listener {
        /**
         * When an emoji is selected on the picker.
         *
         * @param emoji The selected emoji
         */
        fun onEmojiAdded(emoji: Emoji)

        /**
         * When backspace key is clicked.
         *
         * @param keyCode The key code pressed as defined
         *
         * @see android.view.KeyEvent
         */
        fun onNonEmojiKeyPressed(keyCode: Int)
    }
}