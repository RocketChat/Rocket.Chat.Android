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

    // This is just a sample showing 8 chat rooms (aka user subscription). We need to get it rid in a real word. REMARK: remove this comment and this method.
    private fun createDumpData(): List<Chat> {
        val dumpChat1 = Chat("https://open.rocket.chat/avatar/leonardo.aramaki",
                "Leonardo Aramaki",
                "d",
                "busy",
                "Type something",
                LocalDateTime.of(2017, 11, 21,1, 3),
                1)
        val dumpChat2 = Chat("https://open.rocket.chat/avatar/filipe.brito",
                "Filipe Brito",
                "d",
                "online",
                "Type something...Type something...Type something",
                LocalDateTime.of(2017, 11, 22,1, 3),
                150)
        val dumpChat3 = Chat("https://open.rocket.chat/avatar/lucio.maciel",
                "Lucio Maciel",
                "d",
                "away",
                "Type something",
                LocalDateTime.of(2017, 11, 17,1, 3),
                0)
        val dumpChat4 = Chat("https://open.rocket.chat/avatar/sing.li",
                "mobile-internal",
                "p",
                null,
                "@aaron.ogle @rafael.kellermann same problem over here. Although all the servers show up on the selection.",
                LocalDateTime.of(2017, 11, 15,1, 3),
                0)
        val dumpChat5 = Chat("https://open.rocket.chat/avatar/hetal",
                "general",
                "c",
                null,
                "Has joined the channel.",
                LocalDateTime.of(2017, 11, 13,1, 3),
                0)
        val dumpChat6 = Chat("https://open.rocket.chat/avatar/matheus.cardoso",
                "androidnativeapp",
                "c",
                null,
                "Yes @sttyru, but you'll need to implement from the ground up following the docs at docs.rocket.chat where you can see the REST (HTTP) and Real-Time (WebSockets) calls.",
                LocalDateTime.of(2017, 11, 14,1, 3),
                0)
        val dumpChat7 = Chat("https://open.rocket.chat/avatar/bestrun",
                "androidnativeapp-2",
                "c",
                null,
                "Just downloaded .zip and imported into Android Studio then build the project.",
                LocalDateTime.of(2017, 11, 4,12, 47),
                0)
        val dumpChat8 = Chat("https://open.rocket.chat/avatar/3djc",
                "iosnativeapp",
                "c",
                null,
                "Ok, got confused by the blog announcement that shows a screenshot with github oAuth ! Sorry !",
                LocalDateTime.of(2017, 11, 4,12, 43),
                0)

        // creates a list of chat sorted by lastMessageDateTime attribute.
        return listOf(dumpChat1, dumpChat2, dumpChat3, dumpChat4, dumpChat5, dumpChat6, dumpChat7, dumpChat8).sortedByDescending { chat -> chat.lastMessageDateTime }
    }

    // REMARK: The presenter should call this method. The presenter also need to sort the chat list by latest message (compared by its date).
    private fun showChatList(dataSet: List<Chat>) {
        val context = activity.applicationContext
        recycler_view.adapter = ChatListAdapter(dataSet.toMutableList(), context)
        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
    }
}
