package chat.rocket.android.chatdetails.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.item_detail_option.view.*

class ChatDetailsAdapter(private val context: Context): RecyclerView.Adapter<ChatDetailsAdapter.ViewHolder>() {
    private val options: MutableList<Option> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_detail_option))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(options[position])

    override fun getItemCount(): Int = options.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(option: Option) {
            bindName(option, itemView.name)
            bindIcon(option, itemView.icon)
            itemView.setOnClickListener { option.listener() }
        }
    }

    inner class Option(val name: String, val icon: Int, val listener: () -> Unit)

    fun addOption(name: String, icon: Int, listener: () -> Unit) {
        options.add(Option(name, icon, listener))
        notifyDataSetChanged()
    }

    private fun bindName(option: Option, view: TextView) {
        view.text = option.name
    }

    private fun bindIcon(option: Option, view: ImageView) {
        val drawable = DrawableHelper.getDrawableFromId(option.icon, context)
        drawable.let { image ->
            val mutateDrawable = DrawableHelper.wrapDrawable(image).mutate()
            DrawableHelper.tintDrawable(mutateDrawable, context, R.color.colorPrimaryText)
            view.setImageDrawable(mutateDrawable)
        }
    }
}