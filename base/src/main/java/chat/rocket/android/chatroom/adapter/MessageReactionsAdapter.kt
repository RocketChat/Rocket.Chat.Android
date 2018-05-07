package chat.rocket.android.chatroom.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.ReactionViewModel
import chat.rocket.android.dagger.DaggerLocalComponent
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.widget.emoji.Emoji
import chat.rocket.android.widget.emoji.EmojiListenerAdapter
import chat.rocket.android.widget.emoji.EmojiPickerPopup
import chat.rocket.android.widget.emoji.EmojiReactionListener
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MessageReactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val REACTION_VIEW_TYPE = 0
        private const val ADD_REACTION_VIEW_TYPE = 1
    }

    private val reactions = CopyOnWriteArrayList<ReactionViewModel>()
    var listener: EmojiReactionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            ADD_REACTION_VIEW_TYPE -> {
                view = inflater.inflate(R.layout.item_add_reaction, parent, false)
                AddReactionViewHolder(view, listener)
            }
            else -> {
                view = inflater.inflate(R.layout.item_reaction, parent, false)
                SingleReactionViewHolder(view, listener)
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

    fun contains(reactionShortname: String) =
            reactions.firstOrNull { it.shortname ==  reactionShortname} != null

    class SingleReactionViewHolder(view: View,
                                   private val listener: EmojiReactionListener?)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        @Inject lateinit var localRepository: LocalRepository
        @Volatile lateinit var reaction: ReactionViewModel
        @Volatile
        var clickHandled = false

        init {
            DaggerLocalComponent.builder()
                    .context(itemView.context)
                    .build()
                    .inject(this)
        }

        fun bind(reaction: ReactionViewModel) {
            clickHandled = false
            this.reaction = reaction
            with(itemView) {
                val emojiTextView = findViewById<TextView>(R.id.text_emoji)
                val countTextView = findViewById<TextView>(R.id.text_count)
                emojiTextView.text = reaction.unicode
                countTextView.text = reaction.count.toString()
                val myself = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                if (reaction.usernames.contains(myself)) {
                    val context = itemView.context
                    val resources = context.resources
                    countTextView.setTextColor(resources.getColor(R.color.colorAccent))
                }

                emojiTextView.setOnClickListener(this@SingleReactionViewHolder)
                countTextView.setOnClickListener(this@SingleReactionViewHolder)
            }
        }

        override fun onClick(v: View?) {
            synchronized(this) {
                if (!clickHandled) {
                    clickHandled = true
                    listener?.onReactionTouched(reaction.messageId, reaction.shortname)
                }
            }
        }
    }

    class AddReactionViewHolder(view: View,
                                private val listener: EmojiReactionListener?) : RecyclerView.ViewHolder(view) {
        fun bind(messageId: String) {
            itemView as ImageView
            itemView.setOnClickListener {
                val emojiPickerPopup = EmojiPickerPopup(itemView.context)
                emojiPickerPopup.listener = object : EmojiListenerAdapter() {
                    override fun onEmojiAdded(emoji: Emoji) {
                        listener?.onReactionAdded(messageId, emoji)
                    }
                }
                emojiPickerPopup.show()
            }
        }
    }
}