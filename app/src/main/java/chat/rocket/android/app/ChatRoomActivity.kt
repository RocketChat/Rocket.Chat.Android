package chat.rocket.android.app

import android.os.Bundle
import chat.rocket.android.BaseActivity
import chat.rocket.android.R
import chat.rocket.android.app.chatroom.MessageFragment

class ChatRoomActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        addFragment(MessageFragment(), "MessageFragment", R.id.fragment_container)
    }
}