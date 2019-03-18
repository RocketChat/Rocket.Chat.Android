package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.ReactionUiModel
import chat.rocket.android.dagger.DaggerLocalComponent
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiKeyboardListener
import chat.rocket.android.emoji.EmojiPickerPopup
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.infrastructure.LocalRepository
import com.bumptech.glide.Glide
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
                ReactionViewHolder(view, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ReactionViewHolder) {
            holder.bind(reactions[position])
        } else {
            holder as AddReactionViewHolder
            holder.bind(reactions.first().messageId)
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

    class ReactionViewHolder(
        view: View,
        private val listener: EmojiReactionListener?
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

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
                    // The view at index 0 corresponds to the one to display unicode text emoji.
                    view_flipper_reaction.displayedChild = 0
                    text_emoji.text = reaction.unicode
                } else {
                    // The view at index 1 corresponds to the one to display custom emojis which are images.
                    view_flipper_reaction.displayedChild = 1
                    val glideRequest = if (reaction.url!!.endsWith("gif", true)) {
                        Glide.with(context).asGif()
                    } else {
                        Glide.with(context).asBitmap()
                    }

                    glideRequest.load(reaction.url).into(image_emoji)
                }

                val myself = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
                if (reaction.usernames.contains(myself)) {
                    val context = itemView.context
                    text_count.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                }

                text_count.text = reaction.count.toString()

                view_flipper_reaction.setOnClickListener(this@ReactionViewHolder)
                text_count.setOnClickListener(this@ReactionViewHolder)
                view_flipper_reaction.setOnLongClickListener(this@ReactionViewHolder)
                text_count.setOnLongClickListener(this@ReactionViewHolder)
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

        override fun onLongClick(v: View?): Boolean {
            listener?.onReactionLongClicked(reaction.shortname, reaction.isCustom, reaction.url, reaction.usernames)
            return true
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
