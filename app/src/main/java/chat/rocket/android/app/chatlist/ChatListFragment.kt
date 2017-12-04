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
import org.threeten.bp.LocalDateTime

class ChatListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_chat_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showChatList(createDumpData())
    }

    // This is just a sample showing 3 chat rooms (aka user subscription). We need to get it rid in a real word. REMARK: remove this comment and this method.
    private fun createDumpData(): List<Chat> {
        val dumpChat1 = Chat("https://open.rocket.chat/avatar/briiii",
                "Briana",
                "d",
                "busy",
                "Type something",
                LocalDateTime.of(2017, 11, 21,1, 3),
                1)
        val dumpChat2 = Chat("https://open.rocket.chat/avatar/phillica",
                "phillica",
                "d",
                "online",
                "Type something...Type something...Type something",
                LocalDateTime.of(2017, 11, 22,1, 3),
                150)
        val dumpChat3 = Chat("https://open.rocket.chat/avatar/hcharles",
                "Heather Charles",
                "d",
                "away",
                "Type something",
                LocalDateTime.of(2017, 11, 17,1, 3),
                0)

        // creates a list of chat sorted by lastMessageDateTime attribute.
        return listOf(dumpChat1, dumpChat2, dumpChat3).sortedByDescending { chat -> chat.lastMessageDateTime }
    }

    // REMARK: The presenter should call this method. The presenter also need to sort the chat list by latest message (compared by its date).
    private fun showChatList(dataSet: List<Chat>) {
        val context = activity.applicationContext
        recycler_view.adapter = ChatListAdapter(dataSet.toMutableList(), context)
        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
    }
}