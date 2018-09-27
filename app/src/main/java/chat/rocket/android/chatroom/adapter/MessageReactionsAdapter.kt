package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.ReactionUiModel
import chat.rocket.android.dagger.DaggerLocalComponent
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiKeyboardListener
import chat.rocket.android.emoji.EmojiPickerPopup
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.emoji.internal.GlideApp
import chat.rocket.android.infrastructure.LocalRepository
import kotlinx.android.synthetic.main.item_reaction.view.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MessageReactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val reactions = CopyOnWriteArrayList<ReactionUiModel>()
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

    fun addReactions(reactions: List<ReactionUiModel>) {
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
        reactions.firstOrNull { it.shortname == reactionShortname } != null

    class SingleReactionViewHolder(
        view: View,
        private val listener: EmojiReactionListener?
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        @Inject
        lateinit var localRepository: LocalRepository
        @Volatile
        lateinit var reaction: ReactionUiModel
        @Volatile
        var clickHandled = false

        init {
            DaggerLocalComponent.builder()
                .context(itemView.context)
                .build()
                .inject(this)
        }

        fun bind(reaction: ReactionUiModel) {
            clickHandled = false
            this.reaction = reaction
            with(itemView) {
                if (reaction.url.isNullOrEmpty()) {
                    text_emoji.text = reaction.unicode
                    view_flipper_reaction.displayedChild = 0
                } else {
                    view_flipper_reaction.displayedChild = 1
                    val glideRequest = if (reaction.url!!.endsWith("gif", true)) {
                        GlideApp.with(context).asGif()
                    } else {
                        GlideApp.with(context).asBitmap()
                    }

                    glideRequest.load(reaction.url).into(image_emoji)
                }

                text_count.text = reaction.count.toString()
                val myself = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                if (reaction.usernames.contains(myself)) {
                    val context = itemView.context
                    text_count.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                }

                view_flipper_reaction.setOnClickListener(this@SingleReactionViewHolder)
                text_count.setOnClickListener(this@SingleReactionViewHolder)
            }
        }

        override fun onClick(v: View) {
            synchronized(this) {
                if (!clickHandled) {
                    clickHandled = true
                    listener?.onReactionTouched(reaction.messageId, reaction.shortname)
                }
            }
        }
    }

    class AddReactionViewHolder(
        view: View,
        private val listener: EmojiReactionListener?
    ) : RecyclerView.ViewHolder(view) {

        fun bind(messageId: String) {
            itemView as ImageView
            itemView.setOnClickListener {
                val emojiPickerPopup = EmojiPickerPopup(itemView.context)
                emojiPickerPopup.listener = object : EmojiKeyboardListener {
                    override fun onEmojiAdded(emoji: Emoji) {
                        listener?.onReactionAdded(messageId, emoji)
                    }
                }
                emojiPickerPopup.show()
            }
        }
    }

    companion object {
        private const val REACTION_VIEW_TYPE = 0
        private const val ADD_REACTION_VIEW_TYPE = 1
    }
}
