package chat.rocket.android.weblinks.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.room.weblink.WebLinkEntity
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.textContent
import kotlinx.android.synthetic.main.item_web_link.view.*

class WebLinksAdapter(private val context: Context,
                      private val listener: (WebLinkEntity) -> Unit) : RecyclerView.Adapter<WebLinksAdapter.ViewHolder>() {
    var dataSet: MutableList<WebLinkEntity> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_web_link))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    fun updateWebLinks(newWebLinks: List<WebLinkEntity>) {
        dataSet.clear()
        dataSet.addAll(newWebLinks)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(webLink: WebLinkEntity) = with(itemView) {
            bindLink(webLink, text_link)
            setOnClickListener { listener(webLink) }
        }

        private fun bindLink(webLink: WebLinkEntity, textView: TextView) {
            textView.textContent = webLink.link
        }
    }
}