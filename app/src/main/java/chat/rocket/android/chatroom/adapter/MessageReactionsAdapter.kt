package chat.rocket.android.chatroom.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.MessageReactionsAdapter.MessageReactionsViewHolder
import chat.rocket.android.chatroom.viewmodel.ReactionViewModel
import chat.rocket.android.dagger.DaggerLocalComponent
import chat.rocket.android.infrastructure.LocalRepository
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MessageReactionsAdapter : RecyclerView.Adapter<MessageReactionsViewHolder>() {
    private val reactions = CopyOnWriteArrayList<ReactionViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageReactionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_reaction, parent, false)
        return MessageReactionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageReactionsViewHolder, position: Int) {
        holder.bind(reactions[position])
    }

    override fun getItemCount() = reactions.size

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

    class MessageReactionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
}