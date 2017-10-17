package chat.rocket.android.layouthelper.chatroom

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * ViewHolder for Java models.
 */
abstract class ModelViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * Binds the model.
     */
    abstract fun bind(model: T, autoLoadImage: Boolean)
}