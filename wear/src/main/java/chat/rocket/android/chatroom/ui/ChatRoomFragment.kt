package chat.rocket.android.chatroom.ui

import android.app.Fragment
import android.os.Bundle
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import dagger.android.AndroidInjection
import javax.inject.Inject

class ChatRoomFragment : Fragment(), ChatRoomView {
    @Inject
    lateinit var presenter: ChatRoomPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }
}