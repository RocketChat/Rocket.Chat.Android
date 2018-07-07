package chat.rocket.android.emoji.internal

import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiKeyboardListener
import chat.rocket.android.emoji.EmojiParser
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.emoji.Fitzpatrick
import chat.rocket.android.emoji.R
import kotlinx.android.synthetic.main.emoji_category_layout.view.*
import kotlinx.android.synthetic.main.emoji_row_item.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext


internal class EmojiPagerAdapter(private val listener: EmojiKeyboardListener) : PagerAdapter() {

    private val adapters = hashMapOf<EmojiCategory, EmojiAdapter>()
    private var fitzpatrick: Fitzpatrick = Fitzpatrick.Default

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.emoji_category_layout, container, false)
        with(view) {
            val layoutManager = GridLayoutManager(context, 8)

            val category = EmojiCategory.values()[position]
            emoji_recycler_view.layoutManager = layoutManager
            emoji_recycler_view.setRecycledViewPool(RecyclerView.RecycledViewPool())

            container.addView(view)
            launch(UI) {
                val emojis = if (category != EmojiCategory.RECENTS) {
                    EmojiRepository.getEmojiSequenceByCategory(category)
                } else {
                    sequenceOf(*EmojiRepository.getRecents().toTypedArray())
                }
                val recentEmojiSize = EmojiRepository.getRecents().size
                text_no_recent_emoji.isVisible = category == EmojiCategory.RECENTS && recentEmojiSize == 0
                if (adapters[category] == null) {
                    val adapter = EmojiAdapter(listener = listener)
                    emoji_recycler_view.adapter = adapter
                    adapters[category] = adapter
                    adapter.addEmojisFromSequence(emojis)
                }
                adapters[category]!!.setFitzpatrick(fitzpatrick)
            }
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount() = EmojiCategory.values().size

    override fun getPageTitle(position: Int) = EmojiCategory.values()[position].textIcon()

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun setFitzpatrick(fitzpatrick: Fitzpatrick) {
        this.fitzpatrick = fitzpatrick
        for (entry in adapters.entries) {
            if (entry.key != EmojiCategory.RECENTS) {
                entry.value.setFitzpatrick(fitzpatrick)
            }
        }
    }

    class EmojiAdapter(
        private var fitzpatrick: Fitzpatrick = Fitzpatrick.Default,
        private val listener: EmojiKeyboardListener
    ) : RecyclerView.Adapter<EmojiRowViewHolder>() {

        private val emojis = mutableListOf<Emoji>()

        fun addEmojis(emojis: List<Emoji>) {
            this.emojis.clear()
            this.emojis.addAll(emojis)
            notifyDataSetChanged()
        }

        suspend fun addEmojisFromSequence(emojiSequence: Sequence<Emoji>) {
            withContext(CommonPool) {
                emojiSequence.forEachIndexed { index, emoji ->
                    withContext(UI) {
                        emojis.add(emoji)
                        notifyItemInserted(index)
                    }
                }
            }
        }

        fun setFitzpatrick(fitzpatrick: Fitzpatrick) {
            this.fitzpatrick = fitzpatrick
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: EmojiRowViewHolder, position: Int) {
            val emoji = emojis[position]
            holder.bind(
                emoji.siblings.find { it.fitzpatrick == fitzpatrick } ?: emoji
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiRowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.emoji_row_item, parent, false)
            return EmojiRowViewHolder(view, listener)
        }

        override fun getItemCount(): Int = emojis.size
    }

    class EmojiRowViewHolder(
        itemView: View,
        private val listener: EmojiKeyboardListener
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(emoji: Emoji) {
            with(itemView) {
                val parsedUnicode = unicodeCache[emoji.unicode]
                emoji_view.setSpannableFactory(spannableFactory)
                emoji_view.text = if (parsedUnicode == null) {
                    EmojiParser.parse(emoji.unicode, spannableFactory).let {
                        unicodeCache[emoji.unicode] = it
                        it
                    }
                } else {
                    parsedUnicode
                }
                itemView.setOnClickListener {
                    listener.onEmojiAdded(emoji)
                }
            }
        }

        companion object {
            private val spannableFactory = Spannable.Factory()
            private val unicodeCache = mutableMapOf<CharSequence, CharSequence>()
        }
    }
}