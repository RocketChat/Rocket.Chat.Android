package chat.rocket.android.chatrooms.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolder<T : ItemHolder<*>>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    var data: T? = null

    fun bind(data: T) {
        this.data = data
        bindViews(data)
    }

    abstract fun bindViews(data: T)
}