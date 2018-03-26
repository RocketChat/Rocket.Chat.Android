package chat.rocket.android.widget.emoji

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import chat.rocket.android.R


class EmojiPickerPopup(context: Context) : Dialog(context) {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    var listener: EmojiKeyboardListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.emoji_picker)

        viewPager = findViewById(R.id.pager_categories)
        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
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
        viewPager.adapter = CategoryPagerAdapter(object : EmojiListenerAdapter() {
            override fun onEmojiAdded(emoji: Emoji) {
                EmojiRepository.addToRecents(emoji)
                dismiss()
                listener?.onEmojiAdded(emoji)
            }
        })

        for (category in EmojiCategory.values()) {
            val tab = tabLayout.getTabAt(category.ordinal)
            val tabView = LayoutInflater.from(context).inflate(R.layout.emoji_picker_tab, null)
            tab?.customView = tabView
            val textView = tabView.findViewById(R.id.image_category) as ImageView
            textView.setImageResource(category.resourceIcon())
        }

        val currentTab = if (EmojiRepository.getRecents().isEmpty()) EmojiCategory.PEOPLE.ordinal else
            EmojiCategory.RECENTS.ordinal
        viewPager.currentItem = currentTab
    }
}