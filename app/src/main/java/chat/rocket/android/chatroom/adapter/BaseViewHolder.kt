package chat.rocket.android.chatroom.adapter

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var data: T? = null

    fun bind(data: T) {
        this.data = data
        bindViews(data)
    }

    abstract fun bindViews(data: T)
}