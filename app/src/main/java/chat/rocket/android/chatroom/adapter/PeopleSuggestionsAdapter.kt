package chat.rocket.android.chatroom.adapter

import DrawableHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.PeopleSuggestionsAdapter.PeopleSuggestionViewHolder
import chat.rocket.android.chatroom.viewmodel.suggestion.PeopleSuggestionViewModel
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.autocompletion.model.SuggestionModel
import chat.rocket.android.widget.autocompletion.ui.BaseSuggestionViewHolder
import chat.rocket.android.widget.autocompletion.ui.SuggestionsAdapter
import chat.rocket.common.model.UserStatus
import com.facebook.drawee.view.SimpleDraweeView

class PeopleSuggestionsAdapter : SuggestionsAdapter<PeopleSuggestionViewHolder>("@") {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleSuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.suggestion_member_item, parent,
                false)
        return PeopleSuggestionViewHolder(view)
    }

    class PeopleSuggestionViewHolder(view: View) : BaseSuggestionViewHolder(view) {

        override fun bind(item: SuggestionModel, itemClickListener: SuggestionsAdapter.ItemClickListener?) {
            item as PeopleSuggestionViewModel
            with(itemView) {
                val username = itemView.findViewById<TextView>(R.id.text_username)
                val name = itemView.findViewById<TextView>(R.id.text_name)
                val avatar = itemView.findViewById<SimpleDraweeView>(R.id.image_avatar)
                val statusView = itemView.findViewById<ImageView>(R.id.image_status)
                username.text = item.username
                name.text = item.name
                if (item.imageUri.isEmpty()) {
                    avatar.setVisible(false)
                } else {
                    avatar.setVisible(true)
                    avatar.setImageURI(item.imageUri)
                }
                val status = item.status ?: UserStatus.Offline()
                val statusDrawable = DrawableHelper.getUserStatusDrawable(status, itemView.context)
                statusView.setImageDrawable(statusDrawable)
                setOnClickListener {
                    itemClickListener?.onClick(item)
                }
            }
        }
    }
}