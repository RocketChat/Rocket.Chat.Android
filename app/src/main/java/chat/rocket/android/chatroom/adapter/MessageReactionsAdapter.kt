package chat.rocket.android.chatroom.adapter

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.ReactionViewModel
import chat.rocket.android.dagger.DaggerLocalComponent
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.widget.emoji.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MessageReactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val REACTION_VIEW_TYPE = 0
        private const val ADD_REACTION_VIEW_TYPE = 1
    }

    private val reactions = CopyOnWriteArrayList<ReactionViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            ADD_REACTION_VIEW_TYPE -> {
                view = inflater.inflate(R.layout.item_add_reaction, parent, false)
                AddReactionViewHolder(view)
            }
            else -> {
                view = inflater.inflate(R.layout.item_reaction, parent, false)
                SingleReactionViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SingleReactionViewHolder) {
            holder.bind(reactions[position])
        } else {
            holder as AddReactionViewHolder
            holder.bind(reactions[0].messageId)
        }
    }

    override fun getItemCount() = if (reactions.isEmpty()) 0 else reactions.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position == reactions.size) {
            return ADD_REACTION_VIEW_TYPE
        }
        return REACTION_VIEW_TYPE
    }

    fun addReactions(reactions: List<ReactionViewModel>) {
        this.reactions.clear()
        this.reactions.addAllAbsent(reactions)
        notifyItemRangeInserted(0, reactions.size)
    }

    fun clear() {
        val oldSize = reactions.size
        reactions.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    class SingleReactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @Inject lateinit var localRepository: LocalRepository

        init {
            DaggerLocalComponent.builder()
                    .context(itemView.context)
                    .build()
                    .inject(this)
        }

        fun bind(reaction: ReactionViewModel) {
            with(itemView) {
                val emojiTextView = findViewById<TextView>(R.id.text_emoji)
                val countTextView = findViewById<TextView>(R.id.text_count)
                emojiTextView.text = reaction.shortname
                countTextView.text = reaction.count.toString()
                val myself = localRepository.get(LocalRepository.USERNAME_KEY)
                if (reaction.usernames.contains(myself)) {
                    val context = itemView.context
                    val resources = context.resources
                    countTextView.setTextColor(resources.getColor(R.color.colorAccent))
                }
            }
        }
    }

    class AddReactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var viewPager: ViewPager
        private lateinit var tabLayout: TabLayout

        fun bind(messageId: String) {
            itemView as ImageView
            itemView.setOnClickListener {
                val ep = EmojiPickerPopup(itemView.context)
                ep.show()
            }
        }

        private fun setupViewPager() {
            itemView.context.let {
                viewPager.adapter = CategoryPagerAdapter(object : EmojiKeyboardPopup.Listener {
                    override fun onNonEmojiKeyPressed(keyCode: Int) {
                        // do nothing
                    }

                    override fun onEmojiAdded(emoji: Emoji) {
                        EmojiRepository.addToRecents(emoji)
                    }
                })

                for (category in EmojiCategory.values()) {
                    val tab = tabLayout.getTabAt(category.ordinal)
                    val tabView = LayoutInflater.from(it).inflate(R.layout.emoji_picker_tab, null)
                    tab?.setCustomView(tabView)
                    val textView = tabView.findViewById(R.id.image_category) as ImageView
                    textView.setImageResource(category.resourceIcon())
                }

                val currentTab = if (EmojiRepository.getRecents().isEmpty()) EmojiCategory.PEOPLE.ordinal else
                    EmojiCategory.RECENTS.ordinal
                viewPager.setCurrentItem(currentTab)
            }
        }
    }
}