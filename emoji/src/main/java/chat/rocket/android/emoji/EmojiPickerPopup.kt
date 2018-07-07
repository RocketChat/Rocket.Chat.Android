package chat.rocket.android.emoji

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import kotlinx.android.synthetic.main.emoji_picker.*


class EmojiPickerPopup(context: Context) : Dialog(context) {

    var listener: EmojiKeyboardListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.emoji_picker)

        tabs.setupWithViewPager(pager_categories)
        setupViewPager()
        setSize()
    }

    private fun setSize() {
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window.attributes)
        val dialogWidth = lp.width
        val dialogHeight = context.resources.getDimensionPixelSize(R.dimen.picker_popup_height)
        window.setLayout(dialogWidth, dialogHeight)
    }

    private fun setupViewPager() {
        pager_categories.adapter = CategoryPagerAdapter(object : EmojiKeyboardListener {
            override fun onEmojiAdded(emoji: Emoji) {
                EmojiRepository.addToRecents(emoji)
                dismiss()
                listener?.onEmojiAdded(emoji)
            }
        })

        for (category in EmojiCategory.values()) {
            val tab = tabs.getTabAt(category.ordinal)
            val tabView = LayoutInflater.from(context).inflate(R.layout.emoji_picker_tab, null)
            tab?.customView = tabView
            val textView = tabView.findViewById(R.id.image_category) as ImageView
            textView.setImageResource(category.resourceIcon())
        }

        val currentTab = if (EmojiRepository.getRecents().isEmpty()) EmojiCategory.PEOPLE.ordinal else
            EmojiCategory.RECENTS.ordinal
        pager_categories.currentItem = currentTab
    }
}