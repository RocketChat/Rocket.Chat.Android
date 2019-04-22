package chat.rocket.android.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import chat.rocket.common.model.UserStatus
import kotlinx.android.synthetic.main.item_change_status.view.*

class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(listener: (UserStatus) -> Unit) {
        with(itemView) {
            text_online.setOnClickListener { listener(UserStatus.Online()) }
            text_away.setOnClickListener { listener(UserStatus.Away()) }
            text_busy.setOnClickListener { listener(UserStatus.Busy()) }
            text_invisible.setOnClickListener { listener(UserStatus.Offline()) }
        }
    }
}