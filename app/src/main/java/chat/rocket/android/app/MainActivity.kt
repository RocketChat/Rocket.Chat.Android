package chat.rocket.android.app

import android.os.Bundle
import chat.rocket.android.BaseActivity
import chat.rocket.android.R
import chat.rocket.android.app.chatlist.ChatListFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment(ChatListFragment(), "ChatListFragment", R.id.fragment_container)
    }
}