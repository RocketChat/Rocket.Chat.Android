package chat.rocket.android.weblinks.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.room.weblink.WebLinkDao
import chat.rocket.android.room.weblink.WebLinkEntity
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.textContent
import com.facebook.drawee.view.SimpleDraweeView
import com.leocardz.link.preview.library.LinkPreviewCallback
import com.leocardz.link.preview.library.SourceContent
import com.leocardz.link.preview.library.TextCrawler
import kotlinx.android.synthetic.main.item_web_link.view.*
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject


class WebLinksAdapter(private val context: Context,
                      private val listener: (WebLinkEntity) -> Unit) : RecyclerView.Adapter<WebLinksAdapter.ViewHolder>() {

    @Inject
    lateinit var webLinkDao: WebLinkDao

    init {
        DaggerAppComponent.builder().application(RocketChatApplication.application).build().inject(this)
    }

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

        private lateinit var webLinkEntity: WebLinkEntity

        fun bind(webLink: WebLinkEntity) = with(itemView) {
            webLinkEntity = webLink
            bindLink(webLink.link, text_link)
            setOnClickListener { listener(webLink) }

            val title = webLink.title
            val description = webLink.description
            val imageUrl = webLink.imageUrl
            val link = webLink.link

            updateUI(title, text_title,
                    description, text_description,
                    imageUrl, image_web_link)

            if (title.isEmpty() || description.isEmpty() || imageUrl.isEmpty())
                previewFromInternet(link, text_title, text_description, image_web_link)
        }

        private fun previewFromInternet(link: String, textViewTitle: TextView, textViewDescription: TextView, imageView: SimpleDraweeView) {
            val linkPreviewCallback = object : LinkPreviewCallback {

                override fun onPre() {
                    //Do nothing
                }

                override fun onPos(sourceContent: SourceContent?, b: Boolean) {
                    sourceContent?.let {
                        val title = sourceContent.title
                        val description = sourceContent.description
                        val imageList = sourceContent.images
                        var imageUrl = ""

                        if (imageList != null && imageList.size != 0) {
                            imageUrl = imageList[0]
                        }

                        updateUI(sourceContent.title, textViewTitle,
                                sourceContent.description, textViewDescription,
                                imageUrl, imageView)

                        webLinkEntity = WebLinkEntity(title, description, imageUrl, link)

                        launch {
                            webLinkDao.updateWebLink(webLinkEntity)
                        }
                    }
                }
            }
            val textCrawler = TextCrawler()
            textCrawler.makePreview(linkPreviewCallback, link)
        }

        private fun updateUI(title: String, textViewTitle: TextView,
                             description: String, textViewDescription: TextView,
                             imageUrl: String, imageView: SimpleDraweeView) {

            if (!title.isEmpty()) {
                textViewTitle.visibility = View.VISIBLE
                textViewTitle.content = title
            }

            if (!description.isEmpty()) {
                textViewDescription.visibility = View.VISIBLE
                textViewDescription.content = description
            }

            if (title.isEmpty() && !description.isEmpty()) {
                textViewDescription.visibility = View.GONE
                textViewTitle.visibility = View.VISIBLE
                textViewTitle.content = description
            }

            if (!imageUrl.isEmpty()) {
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(imageUrl)
            } else {
                imageView.setActualImageResource(R.drawable.ic_link_black_24dp)
            }
        }

        private fun bindLink(link: String, textView: TextView) {
            textView.textContent = link
        }
    }
}