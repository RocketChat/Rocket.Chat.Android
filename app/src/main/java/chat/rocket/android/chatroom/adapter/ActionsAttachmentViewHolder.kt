package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.uimodel.ActionsAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ButtonAction
import kotlinx.android.synthetic.main.item_actions_attachment.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import timber.log.Timber

class ActionsAttachmentViewHolder(
        itemView: View,
        listener: ActionsListener,
        reactionListener: EmojiReactionListener? = null,
        var actionAttachmentOnClickListener: ActionAttachmentOnClickListener
) : BaseViewHolder<ActionsAttachmentUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(actions_attachment_container)
        }
    }

    override fun bindViews(data: ActionsAttachmentUiModel) {
        val actions = data.actions
        val alignment = data.buttonAlignment
        Timber.d("no of actions : ${actions.size} : $actions")
        with(itemView) {
            title.text = data.title ?: ""
            actions_list.layoutManager = LinearLayoutManager(itemView.context,
                    when (alignment) {
                        "horizontal" -> LinearLayoutManager.HORIZONTAL
                        else -> LinearLayoutManager.VERTICAL //Default
                    }, false)
            actions_list.adapter = ActionsListAdapter(actions, actionAttachmentOnClickListener)
        }
    }
}

interface ActionAttachmentOnClickListener {
    fun onActionClicked(view: View, action: Action)
}