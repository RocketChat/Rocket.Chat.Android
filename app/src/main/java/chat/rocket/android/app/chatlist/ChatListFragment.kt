package chat.rocket.android.app.chatlist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.app.User
import kotlinx.android.synthetic.main.fragment_chat_list.*
import org.threeten.bp.LocalDateTime

class ChatListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_chat_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showChatList(createDumpData())
    }

    // This is just a sample showing 8 chat rooms (aka user subscription). We need to get it rid in a real word. REMARK: remove this comment and this method.
    private fun createDumpData(): List<Chat> {
        val filipe = User("1", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val lucio = User("2", "Lucio Maciel", "Lucio Maciel", "busy", "https://open.rocket.chat/avatar/lucio.maciel")
        val leonardo = User("3", "Leonardo Aramaki", "leonardo.aramaki", "busy", "https://open.rocket.chat/avatar/leonardo.aramaki")
        val sing = User("4", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val hetal = User("5", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val matheus = User("6", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val bestrun = User("7", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val djc = User("8", "3djc", "3djc", "online", "https://open.rocket.chat/avatar/filipe.brito")

        val dumpChat1 = Chat(leonardo,
                "Leonardo Aramaki",
                "d",
                "Type something",
                LocalDateTime.of(2017, 11, 21,1, 3),
                1)
        val dumpChat2 = Chat(filipe,
                "Filipe Brito",
                "d",
                "Type something...Type something...Type something",
                LocalDateTime.of(2017, 11, 22,1, 3),
                150)
        val dumpChat3 = Chat(lucio,
                "Lucio Maciel",
                "d",
                "Type something",
                LocalDateTime.of(2017, 11, 17,1, 3),
                0)
        val dumpChat4 = Chat(sing,
                "mobile-internal",
                "p",
                "@aaron.ogle @rafael.kellermann same problem over here. Although all the servers show up on the selection.",
                LocalDateTime.of(2017, 11, 15,1, 3),
                0)
        val dumpChat5 = Chat(hetal,
                "general",
                "c",
                "Has joined the channel.",
                LocalDateTime.of(2017, 11, 13,1, 3),
                0)
        val dumpChat6 = Chat(matheus,
                "androidnativeapp",
                "c",
                "Yes @sttyru, but you'll need to implement from the ground up following the docs at docs.rocket.chat where you can see the REST (HTTP) and Real-Time (WebSockets) calls.",
                LocalDateTime.of(2017, 11, 14,1, 3),
                0)
        val dumpChat7 = Chat(bestrun,
                "androidnativeapp-2",
                "c",
                "Just downloaded .zip and imported into Android Studio then build the project.",
                LocalDateTime.of(2017, 11, 4,12, 47),
                0)
        val dumpChat8 = Chat(djc,
                "iosnativeapp",
                "c",
                "Ok, got confused by the blog announcement that shows a screenshot with github oAuth ! Sorry !",
                LocalDateTime.of(2017, 11, 4,12, 43),
                0)

        // creates a list of chat sorted by lastMessageDateTime attribute.
        return listOf(dumpChat1, dumpChat2, dumpChat3, dumpChat4, dumpChat5, dumpChat6, dumpChat7, dumpChat8).sortedByDescending { chat -> chat.lastMessageDateTime }
    }

    // REMARK: The presenter should call this method. The presenter also need to sort the chat list by latest message (compared by its date).
    private fun showChatList(dataSet: List<Chat>) {
        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recycler_view.adapter = ChatListAdapter(dataSet.toMutableList(), this)
        }
    }
}
