package chat.rocket.android.app.chatlist

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_chat_list.*

class ChatListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_chat_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChatList()
    }

    fun showChatList() {
        val dumpUser1 = Chat("https://open.rocket.chat/avatar/filipe.brito", "Filipe Brito", "Type something...Type something...Type something", "11:45", 150)
        val dumpUser2 = Chat("https://open.rocket.chat/avatar/leonardo.aramaki", "Leonardo Aramaki", "Type something", "11:44", 1)
        val dumpUser3 = Chat("https://open.rocket.chat/avatar/lucio.maciel", "Lucio Maciel", "Type something", "11:40", 0)

        val dumpData = arrayListOf(dumpUser1, dumpUser2, dumpUser3)
        val context = activity.applicationContext
        recycler_view.adapter = ChatListAdapter(dumpData, context)
        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
    }
}