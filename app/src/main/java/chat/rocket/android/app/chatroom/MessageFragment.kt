package chat.rocket.android.app.chatroom

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.app.User
import kotlinx.android.synthetic.main.fragment_chat_list.*
import org.threeten.bp.LocalDateTime

class MessageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_message, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMessageList(createDumpData())
    }

    // This is just a sample showing 2 messages in the chat room. We need to get it rid in a real word. REMARK: remove this comment and this method.
    private fun createDumpData(): List<Message> {
        val user1 = User("1", "Filipe Brito", "filipe.brito", "online", "https://open.rocket.chat/avatar/filipe.brito")
        val user2 = User("2", "Lucio Maciel", "Lucio Maciel", "busy", "https://open.rocket.chat/avatar/lucio.maciel")

        val message1 = Message(user1, "This is a multiline chat message from Bertie that will take more than just one line of text. I have sure that everything is amazing!", LocalDateTime.now())
        val message2 = Message(user2, "Great!", LocalDateTime.now().plusHours(1))
        return listOf(message1, message2)
    }

    // REMARK: The presenter should call this method.
    private fun showMessageList(dataSet: List<Message>) {
        activity?.apply {
            recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recycler_view.adapter = MessageListAdapter(dataSet.toMutableList())
        }
    }
}