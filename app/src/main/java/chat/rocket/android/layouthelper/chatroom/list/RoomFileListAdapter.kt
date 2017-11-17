package chat.rocket.android.layouthelper.chatroom.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.DateTime
import chat.rocket.android.helper.Logger
import chat.rocket.android.log.RCLog
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.core.models.Attachment
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.android.synthetic.main.item_room_file.view.*
import java.lang.IllegalArgumentException
import java.sql.Timestamp

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomFileListAdapter(private var dataSet: List<Attachment>) : RecyclerView.Adapter<RoomFileListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = dataSet[position]

        val timestamp: Timestamp?
        try {
            timestamp = Timestamp.valueOf(attachment.timestamp)
            // If we don't have a timestamp we can parse let's be safe and stop here.
            holder.newDay.text = DateTime.fromEpocMs(timestamp.time, DateTime.Format.DATE)
            holder.attachment.appendAttachmentView(attachment, true, false)
        } catch (e: IllegalArgumentException) {
            RCLog.e(e)
            Logger.report(e)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun addDataSet(dataSet: List<Attachment>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newDay: TextView = itemView.day
        val attachment: RocketChatMessageAttachmentsLayout = itemView.attachment
    }
}