package chat.rocket.android.layouthelper.chatroom

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.helper.DateTime
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.core.SyncState
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.android.synthetic.main.item_room_message.view.*

abstract class AbstractMessageViewHolder(itemView: View, protected val hostname: String) : ModelViewHolder<PairedMessage>(itemView) {
    private val dayLayout: View by lazy { itemView.dayLayout }
    private val errorImage: ImageView by lazy { itemView.errorImage }
    private val middleContainer: ConstraintLayout by lazy { itemView.middleContainer }
    private val day: TextView by lazy { itemView.day }
    val avatar: RocketChatAvatar by lazy { itemView.avatar }
    val realName: TextView by lazy { itemView.realName }
    val username: TextView by lazy { itemView.username }
    val timestamp: TextView by lazy { itemView.timestamp }

    protected abstract fun bindMessage(pairedMessage: PairedMessage, autoLoadImage: Boolean)

    override fun bind(model: PairedMessage, autoLoadImages: Boolean) {
        if (model.target.syncState == SyncState.FAILED) {
            errorImage.visibility = View.VISIBLE
        } else {
            errorImage.visibility = View.GONE
        }

        bindMessage(model, autoLoadImages)
        renderSequentialAndNewDay(model)
    }

    private fun renderSequentialAndNewDay(pairedMessage: PairedMessage) {
        val target = pairedMessage.target
        val nextSibling = pairedMessage.nextSibling
        if (target != null && nextSibling != null) {
            if (target.isGroupable && nextSibling.isGroupable && pairedMessage.hasSameUser()) {
                setSequential(true)
            } else {
                setSequential(false)
            }
        }


        if (!pairedMessage.hasSameDate()) {
            showNewDay(DateTime.fromEpocMs(pairedMessage.target.timestamp, DateTime.Format.DATE))
        } else {
            showNewDay(null)
        }
    }

    private fun showNewDay(text: String?) {
        if (text.isNullOrEmpty()) {
            dayLayout.visibility = View.GONE
        } else {
            day.text = text
            dayLayout.visibility = View.VISIBLE
        }
    }

    private fun setSequential(showSequential: Boolean) {
        if (showSequential) {
            avatar.visibility = View.GONE
            middleContainer.visibility = View.GONE
        } else {
            avatar.visibility = View.VISIBLE
            middleContainer.visibility = View.VISIBLE
        }
    }
}