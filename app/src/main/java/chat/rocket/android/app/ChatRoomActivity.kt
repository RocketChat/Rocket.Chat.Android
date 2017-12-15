package chat.rocket.android.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.app.chatroom.MessageFragment
import chat.rocket.android.util.addFragment

class ChatRoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        addFragment("MessageFragment", R.id.fragment_container) {
            MessageFragment()
        }
    }
}