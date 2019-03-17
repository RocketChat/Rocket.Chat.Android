package chat.rocket.android.chatdetails.adapter

import DrawableHelper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.chatdetails.domain.Option
import chat.rocket.android.chatrooms.adapter.ViewHolder
import kotlinx.android.synthetic.main.item_detail_option.view.*

class OptionViewHolder(
    itemView: View
): ViewHolder<OptionItemHolder>(itemView) {

    override fun bindViews(data: OptionItemHolder) {
        val option = data.data
        bindName(option, itemView.name)
        bindIcon(option, itemView.icon)
        itemView.setOnClickListener { option.listener() }
    }

    private fun bindIcon(option: Option, view: ImageView) {
        val drawable = DrawableHelper.getDrawableFromId(option.icon, itemView.context)
        drawable.let { image ->
            val mutateDrawable = DrawableHelper.wrapDrawable(image).mutate()
            DrawableHelper.tintDrawable(mutateDrawable, itemView.context, R.color.colorPrimaryText)
            view.setImageDrawable(mutateDrawable)
        }
    }

    private fun bindName(option: Option, view: TextView) {
        view.text = option.name
    }
}