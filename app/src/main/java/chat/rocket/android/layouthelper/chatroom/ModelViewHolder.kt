package chat.rocket.android.layouthelper.chatroom

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ModelViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(model: T, autoLoadImage: Boolean)
}