package chat.rocket.android.files.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.files.uimodel.FileUiModel
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.item_generic_attachment.view.*

class FilesAdapter(private val listener: (FileUiModel) -> Unit) :
    RecyclerView.Adapter<FilesAdapter.ViewHolder>() {
    private var dataSet: List<FileUiModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesAdapter.ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_generic_attachment))

    override fun onBindViewHolder(holder: FilesAdapter.ViewHolder, position: Int) =
        holder.bind(dataSet[position], listener)

    override fun getItemCount(): Int = dataSet.size

    fun prependData(dataSet: List<FileUiModel>) {
        this.dataSet = dataSet
        notifyItemRangeInserted(0, dataSet.size)
    }

    fun appendData(dataSet: List<FileUiModel>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(fileUiModel: FileUiModel, listener: (FileUiModel) -> Unit) {
            with(itemView) {
                when {
                    fileUiModel.isImage -> {
                        image_file_thumbnail.setImageURI(fileUiModel.url)
                        image_file_media_thumbnail.isVisible = false
                        image_file_thumbnail.isVisible = true
                    }
                    fileUiModel.isMedia -> {
                        image_file_media_thumbnail.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.ic_play_arrow_black_24dp, null
                            )
                        )
                        image_file_thumbnail.isVisible = false
                        image_file_media_thumbnail.isVisible = true
                    }
                    else -> {
                        image_file_media_thumbnail.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.ic_insert_drive_file_black_24dp, null
                            )
                        )
                        image_file_thumbnail.isVisible = false
                        image_file_media_thumbnail.isVisible = true
                    }
                }
                text_file_name.text = fileUiModel.name
                text_uploader.text = fileUiModel.uploader
                text_upload_date.text = fileUiModel.uploadDate
                setOnClickListener { listener(fileUiModel) }
            }
        }
    }
}