package chat.rocket.android.servers.adapter

import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.server.domain.model.Account
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_server.view.*

class ServerViewHolder(itemView: View, private val currentServerUrl: String) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(account: Account) {
        with(itemView) {
            Glide.with(context).load(account.serverLogo).into(image_server)
            text_server_name.text = account.serverName ?: account.serverUrl
            text_server_url.text = account.serverUrl
            image_check.isInvisible = currentServerUrl != account.serverUrl
        }
    }
}