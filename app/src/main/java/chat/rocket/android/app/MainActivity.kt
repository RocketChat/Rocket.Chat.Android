package chat.rocket.android.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.app.chatlist.ChatListFragment
import chat.rocket.android.util.addFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment("ChatListFragment", R.id.fragment_container) {
            ChatListFragment()
        }
    }
}