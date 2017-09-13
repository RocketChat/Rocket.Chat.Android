package chat.rocket.android.layouthelper.chatroom.dialog

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import kotlinx.android.synthetic.main.item_room_member.view.*

class RoomMemberListAdapter(private val dataSet: List<String>) : RecyclerView.Adapter<RoomMemberListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.memberNameText.text = dataSet[position]
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberNameText : TextView = itemView.text_member_name
    }
}