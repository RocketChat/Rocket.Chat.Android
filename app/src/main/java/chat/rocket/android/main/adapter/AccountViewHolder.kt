package chat.rocket.android.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import chat.rocket.android.server.domain.model.Account
import kotlinx.android.synthetic.main.item_account.view.*

class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(account: Account) {
        with(itemView) {
            server_logo.setImageURI(account.serverLogo)
            text_server_url.text = account.serverUrl
            text_username.text = account.userName
        }
    }
}