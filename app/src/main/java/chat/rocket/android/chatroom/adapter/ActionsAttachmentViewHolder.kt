package chat.rocket.android.chatroom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.chatroom.uimodel.ActionsAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ButtonAction
import kotlinx.android.synthetic.main.item_actions_attachment.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.util.TimberLogger
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.button.MaterialButton

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
        TimberLogger.debug("no of actions : ${actions.size} : $actions")
        with(itemView) {
            title.text = data.title ?: ""
            actions_list.layoutManager = LinearLayoutManager(itemView.context)
            actions_list.adapter = ActionsListAdapter(actions, actionAttachmentOnClickListener)
        }
    }
}

interface ActionAttachmentOnClickListener {
    fun onActionClicked(action: Action)
}

class ActionsListAdapter(actions: List<Action>, var actionAttachmentOnClickListener: ActionAttachmentOnClickListener) : RecyclerView.Adapter<ActionsListAdapter.ViewHolder>() {

    var actions: List<Action> = actions

    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        lateinit var action: ButtonAction

        var button: MaterialButton = layout.findViewById(R.id.action_button)
        val image: SimpleDraweeView = layout.findViewById(R.id.action_image_button)

        private val onClickListener = View.OnClickListener {
            actionAttachmentOnClickListener.onActionClicked(action)
        }

        init {
            button.setOnClickListener(onClickListener)
            image.setOnClickListener(onClickListener)
        }

        fun bindAction(action: Action) {
            TimberLogger.debug("action : $action")
            this.action = action as ButtonAction

            //TODO
            if (action.imageUrl != null) {
                button.visibility = View.GONE
                image.visibility = View.VISIBLE

                //Image button
                val controller = Fresco.newDraweeControllerBuilder().apply {
                    setUri(action.imageUrl)
                    autoPlayAnimations = true
                    oldController = image.controller
                }.build()
                image.controller = controller

            } else if (action.text != null) {
                button.visibility = View.VISIBLE
                image.visibility = View.GONE

                this.button.setText(action.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_action_button, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = actions[position]
        holder.bindAction(action)
    }
}