package chat.rocket.android.chatroom.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ButtonAction
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.item_action_button.view.*
import timber.log.Timber

class ActionsListAdapter(
    actions: List<Action>,
    var actionAttachmentOnClickListener: ActionAttachmentOnClickListener
) : RecyclerView.Adapter<ActionsListAdapter.ViewHolder>() {

    var actions: List<Action> = actions

    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        lateinit var action: ButtonAction

        private val onClickListener = View.OnClickListener {
            actionAttachmentOnClickListener.onActionClicked(it, action)
        }

        init {
            with(itemView) {
                action_button.setOnClickListener(onClickListener)
                action_image_button.setOnClickListener(onClickListener)
            }
        }

        fun bindAction(action: Action) {
            with(itemView) {
                Timber.d("action : $action")
                this@ViewHolder.action = action as ButtonAction

                if (action.imageUrl != null) {
                    action_button.isVisible = false
                    action_image_button.isVisible = true

                    //Image button
                    val controller = Fresco.newDraweeControllerBuilder().apply {
                        setUri(action.imageUrl)
                        autoPlayAnimations = true
                        oldController = action_image_button.controller
                    }.build()
                    action_image_button.controller = controller

                } else if (action.text != null) {
                    action_button.isVisible = true
                    action_image_button.isVisible = false

                    this.action_button.text = action.text
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_action_button)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = actions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = actions[position]
        holder.bindAction(action)
    }
}
