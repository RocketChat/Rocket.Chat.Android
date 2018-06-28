package chat.rocket.android.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.emoji_category_layout.view.*
import java.util.*

internal class CategoryPagerAdapter(private val listener: EmojiKeyboardListener) : PagerAdapter() {

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.emoji_category_layout, container, false)
        with(view) {
            val layoutManager = GridLayoutManager(context, 8)
            val adapter = EmojiAdapter(layoutManager.spanCount, listener)
            val category = EmojiCategory.values()[position]
            val emojis = if (category != EmojiCategory.RECENTS) {
                EmojiRepository.getEmojisByCategory(category)
            } else {
                EmojiRepository.getRecents()
            }
            val recentEmojiSize = EmojiRepository.getRecents().size
            text_no_recent_emoji.isVisible = category == EmojiCategory.RECENTS && recentEmojiSize == 0
            adapter.addEmojis(emojis)
            emoji_recycler_view.layoutManager = layoutManager
            emoji_recycler_view.itemAnimator = DefaultItemAnimator()
            emoji_recycler_view.adapter = adapter
            emoji_recycler_view.isNestedScrollingEnabled = false
            container.addView(view)
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount() = EmojiCategory.values().size

    override fun getPageTitle(position: Int) = EmojiCategory.values()[position].textIcon()

    class EmojiAdapter(
        private val spanCount: Int,
        private val listener: EmojiKeyboardListener
    ) : RecyclerView.Adapter<EmojiRowViewHolder>() {

        private var emojis = Collections.emptyList<Emoji>()

        fun addEmojis(emojis: List<Emoji>) {
            this.emojis = emojis
            notifyItemRangeInserted(0, emojis.size)
        }

        override fun onBindViewHolder(holder: EmojiRowViewHolder, position: Int) {
            holder.bind(emojis[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiRowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.emoji_row_item, parent, false)
            return EmojiRowViewHolder(view, itemCount, spanCount, listener)
        }

        override fun getItemCount(): Int = emojis.size
    }

    class EmojiRowViewHolder(
        itemView: View,
        private val itemCount: Int,
        private val spanCount: Int,
        private val listener: EmojiKeyboardListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val emojiView: TextView = itemView.findViewById(R.id.emoji)

        fun bind(emoji: Emoji) {
            val context = itemView.context
            emojiView.text = EmojiParser.parse(emoji.unicode)
            val remainder = itemCount % spanCount
            val lastLineItemCount = if (remainder == 0) spanCount else remainder
            val paddingBottom = context.resources.getDimensionPixelSize(R.dimen.picker_padding_bottom)
            if (adapterPosition >= itemCount - lastLineItemCount) {
                itemView.setPadding(0, 0, 0, paddingBottom)
            }
            itemView.setOnClickListener {
                listener.onEmojiAdded(emoji)
            }
        }
    }
}