package chat.rocket.android.emoji

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.emoji.internal.EmojiPagerAdapter
import chat.rocket.android.emoji.internal.PREF_EMOJI_SKIN_TONE
import kotlinx.android.synthetic.main.emoji_picker.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class EmojiPickerPopup(context: Context) : Dialog(context) {

    var listener: EmojiKeyboardListener? = null
    private lateinit var adapter: EmojiPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.emoji_picker)

        tabs.setupWithViewPager(pager_categories)
        GlobalScope.launch(Dispatchers.Main) {
            setupViewPager()
            setSize()
        }
    }

    private fun setSize() {
        val lp = WindowManager.LayoutParams()
        window?.let {
            lp.copyFrom(it.attributes)
            val dialogWidth = lp.width
            val dialogHeight = context.resources.getDimensionPixelSize(R.dimen.picker_popup_height)
            it.setLayout(dialogWidth, dialogHeight)
        }
    }

    private suspend fun setupViewPager() {
        adapter = EmojiPagerAdapter(object : EmojiKeyboardListener {
            override fun onEmojiAdded(emoji: Emoji) {
                EmojiRepository.addToRecents(emoji)
                dismiss()
                listener?.onEmojiAdded(emoji)
            }
        })

        val sharedPreferences = context.getSharedPreferences("emoji", Context.MODE_PRIVATE)
        sharedPreferences.getString(PREF_EMOJI_SKIN_TONE, "")?.let {
            changeSkinTone(Fitzpatrick.valueOf(it))
        }

        pager_categories.adapter = adapter
        pager_categories.offscreenPageLimit = EmojiCategory.values().size

        for (category in EmojiCategory.values()) {
            val tab = tabs.getTabAt(category.ordinal)
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
        pager_categories.currentItem = currentTab
    }

    private fun changeSkinTone(tone: Fitzpatrick) {
        adapter.setFitzpatrick(tone)
    }
}