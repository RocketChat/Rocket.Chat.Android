package chat.rocket.android.layouthelper.chatroom.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.widget.message.RocketChatMessageLayout
import kotlinx.android.synthetic.main.item_room_file.view.*

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomFileListAdapter(private var dataSet: List<String>) : RecyclerView.Adapter<RoomFileListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileNameLink.setText(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    fun setDataSet(dataSet: List<String>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameLink : RocketChatMessageLayout = itemView.fileLink
    }
}