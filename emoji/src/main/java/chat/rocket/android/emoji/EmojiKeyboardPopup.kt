package chat.rocket.android.emoji

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewpager.widget.ViewPager
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.emoji.internal.EmojiPagerAdapter
import chat.rocket.android.emoji.internal.PREF_EMOJI_SKIN_TONE
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_skin_tone_chooser.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EmojiKeyboardPopup(context: Context, view: View) : OverKeyboardPopupWindow(context, view) {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: View
    private lateinit var backspaceView: View
    private lateinit var parentContainer: ViewGroup
    private lateinit var changeColorView: View
    private lateinit var adapter: EmojiPagerAdapter
    var listener: EmojiKeyboardListener? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.emoji_keyboard, null)
        parentContainer = view.findViewById(R.id.emoji_keyboard_container)
        viewPager = view.findViewById(R.id.pager_categories)
        searchView = view.findViewById(R.id.emoji_search)
        backspaceView = view.findViewById(R.id.emoji_backspace)
        tabLayout = view.findViewById(R.id.tabs)
        changeColorView = view.findViewById(R.id.color_change_view)
        tabLayout.setupWithViewPager(viewPager)
        return view
    }

    override fun onViewCreated(view: View) {
        GlobalScope.launch(Dispatchers.Main) {
            setupViewPager()
            setupBottomBar()
        }
    }

    private fun setupBottomBar() {
        searchView.setOnClickListener {
            //TODO: search not yet implemented
        }

        backspaceView.setOnClickListener {
            listener?.onNonEmojiKeyPressed(KeyEvent.KEYCODE_BACK)
        }

        changeColorView.setOnClickListener {
            showSkinToneChooser()
        }

        val sharedPreferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
        sharedPreferences.getString(PREF_EMOJI_SKIN_TONE, "")?.let {
            changeSkinTone(Fitzpatrick.valueOf(it))
        }
    }

    private fun showSkinToneChooser() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_skin_tone_chooser, null)
        val dialog = AlertDialog.Builder(context, R.style.Dialog)
            .setView(view)
            .setTitle(context.getString(R.string.alert_title_default_skin_tone))
            .setCancelable(true)
            .create()

        with(view) {
            image_view_default_tone.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.Default)
            }

            image_view_light_tone.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.LightTone)
            }

            image_view_medium_light.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.MediumLightTone)
            }

            image_view_medium_tone.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.MediumTone)
            }

            image_view_medium_dark_tone.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.MediumDarkTone)
            }

            image_view_dark_tone.setOnClickListener {
                dialog.dismiss()
                changeSkinTone(Fitzpatrick.DarkTone)
            }
        }

        dialog.show()
    }

    private fun changeSkinTone(tone: Fitzpatrick) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_skin_tone)!!
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, getFitzpatrickColor(tone))
        (changeColorView as ImageView).setImageDrawable(wrappedDrawable)
        adapter.setFitzpatrick(tone)
    }

    @ColorInt
    private fun getFitzpatrickColor(tone: Fitzpatrick): Int {
        val sharedPreferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)

        sharedPreferences.edit {
            putString(PREF_EMOJI_SKIN_TONE, tone.type)
        }

        return when (tone) {
            Fitzpatrick.Default -> ContextCompat.getColor(context, R.color.tone_default)
            Fitzpatrick.LightTone -> ContextCompat.getColor(context, R.color.tone_light)
            Fitzpatrick.MediumLightTone -> ContextCompat.getColor(
                context,
                R.color.tone_medium_light
            )
            Fitzpatrick.MediumTone -> ContextCompat.getColor(context, R.color.tone_medium)
            Fitzpatrick.MediumDarkTone -> ContextCompat.getColor(context, R.color.tone_medium_dark)
            Fitzpatrick.DarkTone -> ContextCompat.getColor(context, R.color.tone_dark)
        }
    }

    private suspend fun setupViewPager() {
        context.let {
            val callback: EmojiKeyboardListener? = when (it) {
                is EmojiKeyboardListener -> it
                else -> {
                    val fragments = (it as AppCompatActivity).supportFragmentManager.fragments
                    if (fragments.size == 0 || fragments[0] !is EmojiKeyboardListener) {
                        // Since the app can arrive in an inconsistent state at this point, do not throw
//                        throw IllegalStateException("activity/fragment should implement Listener interface")
                        null
                    } else {
                        fragments[0] as EmojiKeyboardListener
                    }
                }
            }

            adapter = EmojiPagerAdapter(object : EmojiKeyboardListener {
                override fun onEmojiAdded(emoji: Emoji) {
                    EmojiRepository.addToRecents(emoji)
                    callback?.onEmojiAdded(emoji)
                }
            })

            viewPager.offscreenPageLimit = EmojiCategory.values().size
            viewPager.adapter = adapter

            for (category in EmojiCategory.values()) {
                val tab = tabLayout.getTabAt(category.ordinal)
                val tabView = LayoutInflater.from(context).inflate(R.layout.emoji_picker_tab, null)
                tab?.customView = tabView
                val textView = tabView.findViewById(R.id.image_category) as ImageView
                textView.setImageResource(category.resourceIcon())
            }

            val currentTab = if (EmojiRepository.getRecents().isEmpty()) {
                EmojiCategory.PEOPLE.ordinal
            } else {
                EmojiCategory.RECENTS.ordinal
            }

            viewPager.currentItem = currentTab
        }
    }

    class EmojiTextWatcher(private val editor: EditText) : TextWatcher {
        @Volatile
        private var emojiToRemove = mutableListOf<EmojiTypefaceSpan>()

        override fun afterTextChanged(s: Editable) {
            val message = editor.editableText

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
                val message = editor.editableText
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
}
